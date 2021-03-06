#!/bin/bash
#
# Database setup
#
init_local_postgres() {
    SERVICE_NAME=postgresql

    # check if postgres is already running
    systemctl -q is-active $SERVICE_NAME && return 0

    # Copied from postgresql-setup. Determine default data directory
    PGDATA=$(systemctl show -p Environment "${SERVICE_NAME}.service" \
        | sed 's/^Environment=//' | tr ' ' '\n' \
        | sed -n 's/^PGDATA=//p' | tail -n 1)
    if [ x"$PGDATA" = x ]; then
        echo "failed to find PGDATA setting in ${SERVICE_NAME}.service"
        return 1
    fi

    if ! postgresql-check-db-dir "$PGDATA" >/dev/null; then
        PGSETUP_INITDB_OPTIONS="--auth-host=md5 -E UTF8" postgresql-setup initdb || return 1
    fi

    # ensure that PostgreSQL is running
    systemctl start $SERVICE_NAME || return 1
}

db_name=op-monitor
db_user=opmonitor
db_passwd=$(head -c 24 /dev/urandom | base64 | tr "/+" "_-")

db_admin=opmonitor_admin
db_admin_passwd=$(head -c 24 /dev/urandom | base64 | tr "/+" "_-")

db_properties=/etc/xroad/db.properties
root_properties=/etc/xroad.properties

db_addr=127.0.0.1
db_port=5432
db_url=jdbc:postgresql://$db_addr:$db_port/$db_name
db_initialized=false

die () {
    echo >&2 "$@"
    exit 1
}

local_psql() {
  local cmd="psql -qtAU postgres $*"
  su -l -c "$cmd" postgres
}

remote_psql() {
  psql -h "$db_addr" -p "$db_port" -qtAU postgres "$@"
}

get_prop() {
  crudini --get "$1" '' "$2" 2>/dev/null || echo -n "$3"
}

if  [[ -f ${root_properties} && $(get_prop ${root_properties} postgres.connection.password) != "" ]]
then
  master_passwd=$(crudini --get ${root_properties} '' postgres.connection.password)
  export PGPASSWORD=${master_passwd}
  psql_cmd=remote_psql
else
  psql_cmd=local_psql
  init_local_postgres
fi

if  [[ -f ${db_properties}  && $(get_prop ${db_properties} op-monitor.hibernate.connection.url) != "" ]]
then

  db_url=$(get_prop ${db_properties} op-monitor.hibernate.connection.url)
  db_user=$(get_prop ${db_properties} op-monitor.hibernate.connection.username "$db_user")
  db_passwd=$(get_prop ${db_properties} op-monitor.hibernate.connection.password "$db_passwd")
  tmp_admin=$(get_prop ${root_properties} op-monitor.database.admin_password)
  db_initialized=$(get_prop ${root_properties} op-monitor.database.initialized)

  if [[ "$db_url" =~ jdbc:postgresql://([^/]*)/ ]]; then
    db_addr=${BASH_REMATCH[1]%%:*}
    db_port=${BASH_REMATCH[1]#$db_addr};db_port=${db_port:1};db_port=${db_port:-5432}
  fi

  if [[ -z "$db_initialized" && -z "$master_passwd" ]]; then
    # settings exists, but master password is not set. Assume pre-initialized DB.
    db_initialized=true
    crudini --set "$root_properties" '' op-monitor.database.initialized "true"
  fi

  if [[ -z "$tmp_admin" ]]; then
    if [[ "$db_initialized" = true ]]; then
      echo "ALTER ROLE ${db_admin} WITH PASSWORD '${db_admin_passwd}'" | $psql_cmd
      crudini --set "$root_properties" '' op-monitor.database.admin_password "$db_admin_passwd"
    fi
  else
    db_admin_passwd=$tmp_admin
  fi

fi

if [[ "$db_initialized" != true ]]; then
    echo "no db settings detected, creating db"

    if  ! $psql_cmd --list -F "' '" | grep template1 | awk '{print $3}' | grep -q "UTF8"
    then echo -e "\n\nPostgreSQL is not UTF8 compatible."
      echo -e "Aborting installation! please fix issues and rerun\n\n"
      exit 101
    fi

    if [[ $($psql_cmd <<< "SELECT 1 FROM pg_roles WHERE rolname='${db_admin}'") = "1" ]]
    then
      echo "ALTER ROLE $db_admin WITH PASSWORD '${db_admin_passwd}';" | $psql_cmd
    else
      echo "CREATE ROLE $db_admin LOGIN PASSWORD '${db_admin_passwd}';" | $psql_cmd
    fi

    if [[ $($psql_cmd <<< "SELECT 1 FROM pg_roles WHERE rolname='${db_user}'") = "1" ]]
    then
      echo  "$db_user user exists, skipping role creation"
      echo "ALTER ROLE ${db_user} WITH PASSWORD '${db_passwd}';" | $psql_cmd
    else
      echo "CREATE ROLE ${db_user} LOGIN PASSWORD '${db_passwd}';" | $psql_cmd
    fi

    if [[ $($psql_cmd <<< "SELECT 1 FROM pg_database WHERE datname='${db_name}'") = "1" ]]
    then
      echo "database $db_name exists"
    else
      echo "GRANT ${db_admin} to postgres" | $psql_cmd
      echo "CREATE DATABASE \"$db_name\" OWNER \"$db_admin\" ENCODING 'UTF-8'" | $psql_cmd
    fi

    touch ${db_properties}
    crudini --set ${db_properties} '' op-monitor.hibernate.jdbc.use_streams_for_binary true
    crudini --set ${db_properties} '' op-monitor.hibernate.dialect ee.ria.xroad.common.db.CustomPostgreSQLDialect
    crudini --set ${db_properties} '' op-monitor.hibernate.connection.driver_class org.postgresql.Driver
    crudini --set ${db_properties} '' op-monitor.hibernate.jdbc.batch_size 50
    crudini --set ${db_properties} '' op-monitor.hibernate.connection.url "${db_url}"
    crudini --set ${db_properties} '' op-monitor.hibernate.connection.username  "${db_user}"
    crudini --set ${db_properties} '' op-monitor.hibernate.connection.password "${db_passwd}"
    crudini --set "$root_properties" '' op-monitor.database.admin_password "$db_admin_passwd"
    crudini --set "$root_properties" '' op-monitor.database.initialized "true"
fi

chown xroad:xroad ${db_properties}
chmod 640 ${db_properties}

echo "running ${db_name} database migrations"

cd /usr/share/xroad/db/ || exit 1
LIQUIBASE_HOME=/usr/share/xroad/db /usr/share/xroad/db/liquibase.sh \
  --classpath=/usr/share/xroad/jlib/postgresql.jar:/usr/share/xroad/jlib/common-db.jar \
  --url="${db_url}?dialect=ee.ria.xroad.common.db.CustomPostgreSQLDialect" \
  --changeLogFile=/usr/share/xroad/db/${db_name}-changelog.xml \
  --password="${db_admin_passwd}" \
  --username="${db_admin}" \
  update \
  || die "Connection to database has failed, please check database availability and configuration in ${db_properties} file"

PGPASSWORD=$db_admin_passwd psql -qAt -h "$db_addr" -p "$db_port" -U "$db_admin" -d "$db_name" <<EOF
grant usage on schema public to ${db_user};
grant select, insert, update, delete on all tables in schema public to ${db_user};
grant usage, select, update on all sequences in schema public to ${db_user};
grant execute on all functions in schema public to ${db_user};
EOF

exit 0
