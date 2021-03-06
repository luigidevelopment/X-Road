/**
 * The MIT License
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.restapi.util;

import ee.ria.xroad.common.conf.globalconf.GlobalGroupInfo;
import ee.ria.xroad.common.conf.globalconf.MemberInfo;
import ee.ria.xroad.common.identifier.ClientId;
import ee.ria.xroad.common.identifier.GlobalGroupId;

import org.niis.xroad.restapi.exceptions.WarningDeviation;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test utils for constants and generic object creation
 */
public final class TestUtils {

    // Constants
    public static final String LOCALGROUP = "LOCALGROUP";
    public static final String GLOBALGROUP = "GLOBALGROUP";
    public static final String GLOBALGROUP1 = "GLOBALGROUP1";
    public static final String GLOBALGROUP2 = "GLOBALGROUP2";
    public static final String GLOBALGROUP_CODE1 = "GLOBALGROUP_CODE1";
    public static final String NAME_FOR = "Name for: ";
    public static final String SS0_GET_RANDOM_V1 = "FI:GOV:M1:SS0:getRandom.v1";
    public static final String SS1_GET_RANDOM_V1 = "FI:GOV:M1:SS1:getRandom.v1";
    public static final String SS1_GET_RANDOM_V2 = "FI:GOV:M1:SS1:getRandom.v2";
    public static final String SS1_CALCULATE_PRIME = "FI:GOV:M1:SS1:calculatePrime.v1";
    public static final String URL_HTTPS = "https://foo.bar";
    public static final String URL_HTTP = "http://foo.bar";
    public static final String INSTANCE_FI = "FI";
    public static final String INSTANCE_EE = "EE";
    public static final String MEMBER_CLASS_GOV = "GOV";
    public static final String MEMBER_CLASS_PRO = "PRO";
    public static final String MEMBER_CODE_M1 = "M1";
    public static final String MEMBER_CODE_M2 = "M2";
    public static final String SUBSYSTEM = "SUBSYSTEM";
    public static final String SUBSYSTEM1 = "SS1";
    public static final String SUBSYSTEM2 = "SS2";
    public static final String SUBSYSTEM3 = "SS3";
    public static final String CLIENT_ID_SS1 = "FI:GOV:M1:SS1";
    public static final String CLIENT_ID_SS2 = "FI:GOV:M1:SS2";
    public static final String CLIENT_ID_SS3 = "FI:GOV:M1:SS3";
    public static final String CLIENT_ID_SS4 = "FI:GOV:M1:SS4";
    public static final String NEW_GROUPCODE = "NEW_GROUPCODE";
    public static final String GROUP_DESC = "GROUP_DESC";
    public static final String NEW_GROUP_DESC = "NEW_GROUP_DESC";
    public static final String INVALID_GROUP_ID = "NOT_VALID";
    // values from initial test data: src/test/resources/data.sql
    public static final String DB_GLOBALGROUP_ID = "FI:security-server-owners";
    public static final String DB_GLOBALGROUP_CODE = "security-server-owners";
    public static final String DB_LOCAL_GROUP_ID_1 = "1";
    public static final String DB_LOCAL_GROUP_ID_2 = "2";
    public static final String DB_LOCAL_GROUP_CODE = "group1";
    // services from initial test data: src/test/resources/data.sql
    public static final String SERVICE_XROAD_GET_RANDOM_OLD = "xroadGetRandomOld.v1";
    public static final String SERVICE_BMI_OLD = "bodyMassIndexOld.v1";
    public static final String SERVICE_GET_RANDOM = "getRandom.v1";
    public static final String SERVICE_CALCULATE_PRIME = "calculatePrime.v1";
    // services from wsdl test file: src/test/resources/testservice.wsdl
    public static final String SERVICE_XROAD_GET_RANDOM = "xroadGetRandom.v1";
    public static final String SERVICE_BMI = "bodyMassIndex.v1";

    private TestUtils() {
        // noop
    }

    /**
     * Returns a new ClientId with given params
     * @param instance
     * @param memberClass
     * @param memberCode
     * @param subsystem
     * @return ClientId
     */
    public static ClientId getClientId(String instance, String memberClass, String memberCode, String subsystem) {
        return ClientId.create(instance, memberClass, memberCode, subsystem);
    }

    /**
     * Returns a new ClientId with default parameters "FI:GOV:M1:SS1"
     * @return ClientId
     */
    public static ClientId getM1Ss1ClientId() {
        return getClientId(INSTANCE_FI, MEMBER_CLASS_GOV, MEMBER_CODE_M1, SUBSYSTEM1);
    }

    /**
     * Returns a new MemberInfo with given parameters
     * @param instance
     * @param memberClass
     * @param memberCode
     * @param subsystem
     * @return MemberInfo
     */
    public static MemberInfo getMemberInfo(String instance, String memberClass, String memberCode, String subsystem) {
        return new MemberInfo(getClientId(instance, memberClass, memberCode, subsystem),
                subsystem != null ? NAME_FOR + subsystem : null);
    }

    /**
     * Returns a new GlobalGroupInfo object
     * @param instance
     * @param groupCode
     * @return
     */
    public static GlobalGroupInfo getGlobalGroupInfo(String instance, String groupCode) {
        return new GlobalGroupInfo(GlobalGroupId.create(instance, groupCode), groupCode + "-description");
    }

    /**
     * Finds warning with matching code, or returns null
     * @param code
     * @param warningDeviations
     * @return
     */
    public static WarningDeviation findWarning(String code, Collection<WarningDeviation> warningDeviations) {
        if (warningDeviations != null) {
            return warningDeviations.stream()
                    .filter(warning -> code.equals(warning.getCode()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * assert that path <code>http://http://localhost</code> + endpointPathEnd
     * exists in header <code>Location</code> (true for our integration tests)
     * @param endpointPath for example "/api/service-descriptions/12"
     * @param response
     */
    public static void assertLocationHeader(String endpointPath, ResponseEntity response) {
        assertEquals(Collections.singletonList(TEST_API_URL + endpointPath),
                response.getHeaders().get("Location"));
    }
    private static final String TEST_API_URL = "http://localhost";

    /**
     * assert that request does not have <code>Location</code> headers
     * @param response
     */
    public static void assertMissingLocationHeader(ResponseEntity response) {
        List<String> locationHeaders = response.getHeaders().get("Location");
        assertNull(locationHeaders);
    }


}
