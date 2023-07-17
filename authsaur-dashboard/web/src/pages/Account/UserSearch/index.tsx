import { Select, SelectProps } from "antd";
import React, { useState } from "react";
import { queryFakeUserList } from "./service";


let timeout: ReturnType<typeof setTimeout> | null;
let currentValue: string;

const fetch = (value: string, callback: Function) => {
  if (timeout) {
    clearTimeout(timeout);
    timeout = null;
  }
  currentValue = value;

  const fake = () => {
    queryFakeUserList({ qs: value })
      .then((d: any) => {
        console.log(d, currentValue, value)
        if (currentValue === value) {
          const result = d?.data?.list || [];
          console.log(result)
          const transformData = [];
          result.forEach((item: any) => {
            return transformData.push({
              value: item.id,
              text: item.name + '(' + item.id + ')',
            })
          });
          console.log(transformData)
          callback(transformData);
        }
      });
  };

  timeout = setTimeout(fake, 300);
};


const Demo: React.FC<{}> = (props: { selectValue, onChange }) => {

  const [selectVal, setSelectVal] = useState();
  const [data, setData] = useState<SelectProps['options']>([]);

  React.useEffect(() => {
    setSelectVal(props?.selectValue);
  }, [props?.selectValue]);

  const handleSearch = (newValue: string) => {
    console.log(newValue)
    if (newValue) {
      fetch(newValue, setData);
    } else {
      setData([]);
    }
  };

  const handleChange = (newValue: string) => {
    console.log(newValue)
    setSelectVal(newValue);
    props.onChange(newValue)

  };


  return (
    <Select
      showSearch
      mode="multiple"

      // allowClear
      value={selectVal}
      // fieldProps={{
      //   value:{selectVal}
      // }}
      placeholder="请输入用户名搜索"
      defaultActiveFirstOption={false}
      showArrow={false}
      filterOption={false}
      onSearch={handleSearch}
      onChange={handleChange}
      notFoundContent={null}
      // optionLabelProp="label"
      labelInValue
      options={(data || []).filter((o) => !(selectVal || []).includes(o.value)).map((d) => ({
        value: d.value,
        label: d.text,
      }))}
    />
  );
};

export default Demo;
