import { message, Switch } from "antd";
import React, { useState } from "react";
import { stateFakeItem } from "./service";



const Demo: React.FC<{}> = (props: { source }) => {

  const isChecked = props?.source?.state == 'ACTIVE';
  const id = props?.source?.id;
  console.log(isChecked, id)
  const [lang, setLang] = useState<boolean>(isChecked);
  const [langLoading, setLangLoading] = useState(false);

  return (
    <Switch checked={lang} loading={langLoading} onChange={(checked, e) => {
      setLangLoading(true);
      // setLang(!checked);
      console.log(checked, e)
      stateFakeItem({ id, enabled: checked }).then(function (response) {
        setLangLoading(false);
        if (response.success == true) {
          message.success({ content: '更新成功', className: "box_success" });
          setLang(checked);
          return true;
        } else {
          setLang(!checked);
          message.error({ content: '更新失败:' + response?.msg, className: "box_error" });
          return false;
        }
      })
    }}
    />
  );
};

export default (props) => (
  <div >
    <div >
      <Demo source={props.source} />
    </div>
  </div>
);
