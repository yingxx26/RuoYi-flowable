<template>
  <div>
    <el-form label-width="80px" size="small">
      <el-form-item label="流程表单">
        <el-select v-model="formKey" @change="updateElementFormKey">
          <el-option
            v-for="item in formlist"
            :key="item.formId"
            :label="item.formName"
            :value="item.formId"
          >
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>


  </div>
</template>

<script>

import {addDeployForm, listAllForm} from '@/api/flowable/form'
import {StrUtil} from "@/utils/StrUtil";

export default {
  name: "MyFormPanel",
  props: {
    id: {
      type: String,
      required: true
    },
  },
  data() {
    return {
      formlist: [],
      formKey: String,
    }
  },

  created() {
    this.getListForm();
    this.resetFlowForm();
  },
  methods: {
    getListForm() {
      listAllForm().then(res => {
        this.formlist = res.data
      })
    },
    updateElementFormKey(val) {
      if (StrUtil.isBlank(val)) {
        delete this.modelerStore.element.businessObject[`formKey`]
      } else {
        this.modelerStore.modeling.updateProperties(this.modelerStore.element, {'formKey': val});
      }
    },
    // 方法区
    resetFlowForm() {
      this.formKey = this.modelerStore.element.businessObject.formKey;
    },

    /** 挂载表单 */
    submitFormDeploy(formId){
      this.formDeployParam.formId =  formId;
      addDeployForm(this.formDeployParam).then(res =>{
        this.$modal.msgSuccess(res.msg);
        this.formDeployOpen = false;
      })
    },
  }
}


</script>
