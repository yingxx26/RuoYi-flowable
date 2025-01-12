/**
 * v-hasPermi 操作权限处理
 * Copyright (c) 2019 ruoyi
 */
import store from "@/store";


export default {

  inserted(el, binding, vnode) {
    const {value} = binding;
    const all_pers = "*:*:*";
    const mypers = store.getters?.permissions;
    if (value && value instanceof Array) {
      const labelpers = value;
      let some = mypers.some(per => {
        return all_pers == per || labelpers.includes(per)
      });
      if (!some) {
        el.parentNode?.removeChild(el);
      }
    } else {
      throw new Error("请设置操作权限标签值")
    }
  }

}
