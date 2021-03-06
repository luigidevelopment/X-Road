
<template>
  <div class="view-wrap">
    <subViewTitle class="view-title" :title="$t('csr.generateCsr')" :showClose="false" />
    <v-stepper :alt-labels="true" v-model="currentStep" class="stepper noshadow">
      <v-stepper-header class="noshadow">
        <v-stepper-step :complete="currentStep > 1" step="1">{{$t('csr.csrDetails')}}</v-stepper-step>
        <v-divider></v-divider>
        <v-stepper-step :complete="currentStep > 2" step="2">{{$t('csr.generateCsr')}}</v-stepper-step>
      </v-stepper-header>

      <v-stepper-items class="stepper-content">
        <!-- Step 1 -->
        <v-stepper-content step="1">
          <WizardPageCsrDetails @cancel="cancel" @done="save" />
        </v-stepper-content>
        <!-- Step 2 -->
        <v-stepper-content step="2">
          <WizardPageGenerateCsr @cancel="cancel" @done="done" />
        </v-stepper-content>
      </v-stepper-items>
    </v-stepper>
  </div>
</template>

<script lang="ts">
import Vue from 'vue';
import { mapGetters } from 'vuex';
import HelpIcon from '@/components/ui/HelpIcon.vue';
import LargeButton from '@/components/ui/LargeButton.vue';
import SubViewTitle from '@/components/ui/SubViewTitle.vue';
import WizardPageCsrDetails from './WizardPageCsrDetails.vue';
import WizardPageGenerateCsr from './WizardPageGenerateCsr.vue';

import { Key, Token } from '@/types';
import { RouteName, UsageTypes } from '@/global';
import * as api from '@/util/api';

export default Vue.extend({
  components: {
    HelpIcon,
    LargeButton,
    SubViewTitle,
    WizardPageCsrDetails,
    WizardPageGenerateCsr,
  },
  props: {
    keyId: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      currentStep: 0,
    };
  },
  computed: {
    ...mapGetters(['localMembersIds']),
  },
  methods: {
    save(): void {
      this.$store.dispatch('fetchCsrForm').then(
        (response) => {
          this.currentStep = 2;
        },
        (error) => {
          this.$bus.$emit('show-error', error.message);
        },
      );
    },
    cancel(): void {
      this.$store.dispatch('resetState');
      this.$router.replace({ name: RouteName.SignAndAuthKeys });
    },
    done(): void {
      this.$store.dispatch('resetState');
      this.$router.replace({ name: RouteName.SignAndAuthKeys });
    },

    fetchKeyData(id: string): void {
      this.$store.dispatch('fetchKeyData').catch((error) => {
        this.$bus.$emit('show-error', error.message);
      });
    },

    fetchLocalMembers(): void {
      this.$store.dispatch('fetchLocalMembers').catch((error) => {
        this.$bus.$emit('show-error', error.message);
      });
    },

    fetchCertificateAuthorities(): void {
      this.$store.dispatch('fetchCertificateAuthorities').catch((error) => {
        this.$bus.$emit('show-error', error.message);
      });
    },
  },
  created() {
    this.$store.commit('storeKeyId', this.keyId);
    this.fetchKeyData(this.keyId);
    this.fetchLocalMembers();
    this.fetchCertificateAuthorities();
  },
});
</script>

<style lang="scss" scoped>
@import '../../assets/colors';
@import '../../assets/shared';

.view-wrap {
  width: 100%;
  max-width: 850px;
  margin: 10px;
}

.view-title {
  width: 100%;
  max-width: 100%;
  margin-bottom: 30px;
}

.stepper-content {
  width: 100%;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.stepper {
  width: 100%;
}

.noshadow {
  -webkit-box-shadow: none;
  -moz-box-shadow: none;
  box-shadow: none;
}
</style>