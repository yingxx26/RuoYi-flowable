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
        <el-input-tag v-model="bpmnFormData.assignee"></el-input-tag>
        <el-button-group>
          <el-tooltip>
            <el-button @click="singleUserCheck" size="mini" type="primary" icon="el-icon-user"></el-button>
          </el-tooltip>
          <el-tooltip>
            <el-button @click="singleUserCheck" size="mini" type="warning" icon="el-icon-postcard"></el-button>
          </el-tooltip>
        </el-button-group>

      </el-form-item>

      <el-form-item label="候选人员" v-if="bpmnFormData.userType==='candidateUsers'">
        候选人员
      </el-form-item>

      <el-form-item label="候选角色" v-if="bpmnFormData.userType==='candidateGroups'">
        候选角色
      </el-form-item>
    </el-form>
    <el-dialog title="选择人员" :visible="userVisible"
               :close-on-press-escape="false"
               :show-close="false">
      <MyFlowUser v-if="userVisible" @handleUserSelect="userSelect"></MyFlowUser>
      <div style="display: flex;justify-content: flex-end;margin-top: 20px">
        <el-button size="small" @click="userVisible=false">取 消</el-button>
        <el-button size="small" @click="userVisible=false">确 定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import MyFlowUser from '@/components/flow/MyUser'
import FlowRole from '@/components/flow/Role'
import FlowExp from '@/components/flow/Expression'
import ElInputTag from '@/components/flow/ElInputTag'
import {StrUtil} from '@/utils/StrUtil'
import {expList, roleList, userList} from "@/api/flowable/definition";

export default {
  name: "MyTaskPanel",
  components: {
    MyFlowUser,
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
      userVisible: false,
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
  watch: {
    id: {
      handler(newVal) {
        if (StrUtil.isNotBlank(newVal)) {
          this.resetTaskForm();
        }
      },
      immediate: true
    }
  },
  created() {

  },
  methods: {
    resetTaskForm() {
      // 初始化设为空值
      this.bpmnFormData = {
        userType: "",
        assignee: "",
        candidateUsers: "",
        candidateGroups: "",
        dueDate: "",
        priority: "",
        dataType: "",
        expId: "",
      }
      // 流程节点信息上取值
      for (let key in this.bpmnFormData) {
        const val = this.modelerStore.element?.businessObject[key] || this.bpmnFormData[key];
        this.$set(this.bpmnFormData, key, val)
      }
      // 人员信息回显
      this.checkValuesEcho(this.bpmnFormData);
    },
    // 设计器右侧表单数据回显
    checkValuesEcho(formData) {
      this.getUserList(formData.userType, formData[formData.userType]);
    },

    // 获取人员信息
    getUserList(key,val) {
      if (StrUtil.isNotBlank(val)) {
        let filter = this.modelerStore.userList?.filter(user => (val + "").split(',').includes(user.userId.toString()));
        this.bpmnFormData[key] = filter.map(item => item.nickName).join(',');
      }
    },


    /*单选人员*/
    singleUserCheck() {
      this.userVisible = true;
      this.checkType = "single";
    },
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
    // 用户选中数据 TODO: 后面更改为 点击确认按钮再赋值人员信息
    userSelect(selection) {
      debugger
      if (selection) {
        this.deleteFlowAttar();
        this.updateCustomElement('dataType', 'fixed');
        if (selection instanceof Array) {
          const userIds = selection.map(item => item.userId);
          const nickName = selection.map(item => item.nickName);
          // userType = candidateUsers
          this.bpmnFormData[this.bpmnFormData.userType] = nickName.join(',');
          this.updateCustomElement(this.bpmnFormData.userType, userIds.join(','));
          this.handleSelectData(this.bpmnFormData.userType, userIds);
        } else {
          // userType = assignee
          this.bpmnFormData[this.bpmnFormData.userType] = selection.nickName;
          this.updateCustomElement(this.bpmnFormData.userType, selection.userId);
          this.handleSelectData(this.bpmnFormData.userType, selection.userId);
        }
      }
    },
    // 处理人员回显
    handleSelectData(key, value) {
      for (let oldKey in this.selectData) {
        if (key !== oldKey) {
          this.$set(this.selectData, oldKey, null);
        } else {
          this.$set(this.selectData, oldKey, value);
        }
      }
    },
  }
}
</script>
