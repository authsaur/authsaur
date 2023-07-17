import { Select, SelectProps } from "antd";
import React, { useState } from "react";
import { queryFakeOrgList } from "./service";


let timeout: ReturnType<typeof setTimeout> | null;
let currentValue: string;

const fetch = (value: string, source: string, callback: Function) => {
  if (timeout) {
    clearTimeout(timeout);
    timeout = null;
  }
  currentValue = value;

  const fake = () => {
    queryFakeOrgList({ qs: value, source: source || '' })
      .then((d: any) => {
        // console.log(d, currentValue, value)
        if (currentValue === value) {
          const result = d?.data?.list || [];
          // console.log(result)
          const transformData: { value: any; text: any; }[] = [];
          result.forEach((item: any) => {
            return transformData.push({
              value: item.id,
              text: item.path,
            })
          });
          // console.log(transformData)
          callback(transformData);
        }
      });
  };

  timeout = setTimeout(fake, 300);
};


const Demo: React.FC<{}> = (props: { source, selectValue, onChange }) => {

  const [selectVal, setSelectVal] = useState();
  const [data, setData] = useState<SelectProps['options']>([]);

  React.useEffect(() => {
    setSelectVal(props?.selectValue);
  }, [props?.selectValue]);

  const handleSearch = (newValue: string) => {
    console.log(newValue)
    if (newValue) {
      fetch(newValue, props?.source || '', setData);
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
      placeholder="请输入部门名搜索"
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
