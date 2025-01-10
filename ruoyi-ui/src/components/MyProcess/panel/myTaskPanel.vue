<template>
  <div>
    <el-form label-width="80px" size="small">
      <el-form-item label="异步">
        <el-switch v-model="bpmnFormData.async" active-text="是" inactive-text="否"
                   @change="updateElementTask('async')"></el-switch>
      </el-form-item>
      <el-form-item label="用户类型">
        <el-select v-model="bpmnFormData.userType" @change="updateUserType">
          <el-option
            v-for="item in userTypeOption"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="指定人员" v-if="bpmnFormData.userType==='assignee'">
        指定人员
      </el-form-item>

      <el-form-item label="候选人员" v-if="bpmnFormData.userType==='assignee'">
        候选人员
      </el-form-item>

      <el-form-item label="候选角色" v-if="bpmnFormData.userType==='assignee'">
        候选角色
      </el-form-item>
    </el-form>


  </div>
</template>

<script>
import FlowUser from '@/components/flow/User'
import FlowRole from '@/components/flow/Role'
import FlowExp from '@/components/flow/Expression'
import ElInputTag from '@/components/flow/ElInputTag'
import {StrUtil} from '@/utils/StrUtil'
import {expList, roleList, userList} from "@/api/flowable/definition";

export default {
  name: "MyTaskPanel",
  components: {
    FlowUser,
    FlowRole,
    FlowExp,
    ElInputTag
  },
  /** 组件传值  */
  props: {
    id: {
      type: String,
      required: true
    },
  },
  data() {
    return {
      bpmnFormData: {
        userType: "",
        assignee: "",
        candidateUsers: "",
        candidateGroups: "",
        dueDate: "",
        priority: "",
        dataType: "",
        expId: ""
      },
      userTypeOption: [
        {label: '指定人员', value: 'assignee'},
        {label: '候选人员', value: 'candidateUsers'},
        {label: '候选角色', value: 'candidateGroups'},
      ]
    }
  },

  /** 传值监听 */
  watch: {},
  created() {

  },
  methods: {

    // 更新人员类型
    updateUserType(val) {
      this.deleteFlowAttar();
      delete this.modelerStore.element.businessObject['userType']
      this.bpmnFormData[val] = null;
      this.updateCustomElement('userType', val);
    },

    // 更新节点信息
    updateElementTask: function (key) {
      let taskattr = Object.create(null);
      taskattr[key] = this.bpmnFormData || '';
      this.modelerStore.modeling.updateProperties(this.modelerStore.element, taskattr);
    },
    // 更新自定义流程节点/参数信息
    updateCustomElement(key, value) {
      const taskAttr = Object.create(null);
      taskAttr[key] = value;
      this.modelerStore.modeling.updateProperties(this.modelerStore.element, taskAttr);
    },

    // 删除节点
    deleteFlowAttar() {
      delete this.modelerStore.element.businessObject[`dataType`]
      delete this.modelerStore.element.businessObject[`expId`]
      delete this.modelerStore.element.businessObject[`assignee`]
      delete this.modelerStore.element.businessObject[`candidateUsers`]
      delete this.modelerStore.element.businessObject[`candidateGroups`]
    },
  }
}
</script>
