import { MinusCircleTwoTone, PlusCircleTwoTone, QuestionCircleOutlined, UserOutlined } from '@ant-design/icons';
import { ProColumns, ProTable } from '@ant-design/pro-components';
import { PageContainer } from '@ant-design/pro-layout';
import { Alert, Descriptions, Select, SelectProps, Tag, Tooltip } from 'antd';
import React, { useState } from 'react';
import { request } from 'umi';
import { queryFakeUserList } from '../../Auth/add/service';

import moment from 'moment';

const formatterTime = (val) => {
  return val ? moment(val).format('YYYY-MM-DD HH:mm:ss') : ''
}

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



const CardList = () => {
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([]);
  const [selectVal, setSelectVal] = useState();
  const [data, setData] = useState<SelectProps['options']>([]);

  const handleSearch = (newValue: string) => {
    console.log(newValue)
    if (newValue) {
      fetch(newValue, setData);
    } else {
      setData();
    }
  };

  const handleChange = (newValue: string) => {
    console.log(newValue)
    setSelectVal(newValue);

  };
  const columns: ProColumns<any>[] = [
    {
      dataIndex: 'actionName',
      title: '事件',
      valueType: 'select',
      hideInSearch: true,
      valueEnum: {
        '': { text: '全部' },
        Regex: {
          text: 'CAS',
        },
        SAML: {
          text: 'SAML',
        },
        OAuth: {
          text: 'OAuth2',
        },
        Oidc: {
          text: 'OIDC',
        },
        zentao: {
          text: 'zentao',
        },
      },
    },
    {
      dataIndex: 'principal',
      title: '用户',
      // hideInSearch: true,
      render: (_, record) => (
        record?.principalName ? <Tooltip title={record.principal}><UserOutlined />  {record?.principalName || record?.user?.uname}</Tooltip>
          : record.principal
      ),
      hideInForm: true,
      renderFormItem: (_, { type, defaultRender, ...rest }, form) => {
        return (
          <Select
            key="searchSelect"
            showSearch
            allowClear
            // labelInValue
            showArrow={false}
            notFoundContent={null}
            placeholder="输入用户姓名搜索"
            filterOption={false}
            onSearch={handleSearch}

          >
            {(data || []).map(item => <Option key={item.value}>{item.text}</Option>)}
          </Select >
        )

      },
    },
    {
      dataIndex: 'clientIpAddress',
      title: 'IP',
    },
    {
      title: '时间',
      key: 'showTime',
      dataIndex: 'whenActionWasPerformed',
      // valueType: 'datetime',
      // sorter: true,
      hideInSearch: true,
      render: formatterTime
    },
    {
      title: '时间',
      dataIndex: 'created_at',
      valueType: 'dateTimeRange',
      hideInTable: true,
      search: {
        transform: (value) => {
          return {
            startTime: value[0],
            endTime: value[1],
          };
        },
      },
    },
    {
      dataIndex: 'result',
      title: '结果',
      hideInSearch: true,
      render: (_, record) => (
        (record?.result) ? <Tag color="green">成功</Tag> : <Tag color="red">失败</Tag>
      )
    },
  ];

  return (
    <PageContainer>
      <ProTable<any>
        columns={columns}
        // cardBordered
        // showHeader={false}
        // split={false}
        search={{
          collapsed: false,
          defaultCollapsed: false,
          collapseRender: false,
          layout:'vertical',
        }}
        options={false}
        pagination={{
          defaultPageSize: 10,
          showSizeChanger: false,
        }}
        // cardProps={
        //   {
        //     bodyStyle: { paddingTop: '24px' },
        //   }
        // }
        expandable={{
          expandIcon: ({ expanded, onExpand, record }) =>
            expanded ? (
              <MinusCircleTwoTone onClick={e => onExpand(record, e)} />
            ) : (
              <PlusCircleTwoTone onClick={e => onExpand(record, e)} />
            ),
          expandedRowRender: (record) => {
            let failMessage = ' ';
            if (record.failCode == 'authenticationFailure.AccountNotFoundException') {
              failMessage = "用户不存在或密码错误";
            } else if (record.failCode == 'authenticationFailure.AccountDisabledException') {
              failMessage = "这个账户被禁用了";
            } else if (record.failCode == 'authenticationFailure.InvalidLoginTimeException') {
              failMessage = "账户该时刻被禁止登录了";
            } else if (record.failCode == 'authenticationFailure.FailedLoginException') {
              failMessage = "无效凭证";
            } else if (record.failCode == 'authenticationFailure.UnauthorizedServiceForPrincipalException') {
              failMessage = "没有权限访问目标应用";
            } else if (record.failCode == 'authenticationFailure.UNKNOWN') {
              failMessage = "认证信息无效";
            } else if (record.failCode) {
              failMessage = "这个账户被禁用了";
            }
            return <>
              <Alert
                message={record.actionName + (record.result ? '成功' : ('失败'))}
                description={failMessage}
                type={record?.result ? "success" : "error"}
                showIcon
              />
              <Descriptions colon={true} column={3} style={{ padding: '16px 16px 0 16px' }}>
                <Descriptions.Item label="时间">{formatterTime(record.whenActionWasPerformed)}</Descriptions.Item>
                <Descriptions.Item label="设备类型">{record.device}</Descriptions.Item>
                <Descriptions.Item label="浏览器">{record.browser}</Descriptions.Item>

                <Descriptions.Item label="事件">{record.actionName}</Descriptions.Item>
                <Descriptions.Item label="设备系统">{record.os}</Descriptions.Item>
                <Descriptions.Item label="浏览器版本">{record.browserVer}</Descriptions.Item>

                {record.result ? <Descriptions.Item label="用户名"><Tooltip title={record.principal}>{record?.principalName || record?.user?.uname}  {record?.user?.actualid}  <QuestionCircleOutlined /></Tooltip>
                </Descriptions.Item> : <Descriptions.Item label="用户名">{record.principal}</Descriptions.Item>}
                <Descriptions.Item label="系统版本">{record.osVer}</Descriptions.Item>
                <Descriptions.Item label="客户端IP">{record.clientIpAddress}</Descriptions.Item>

                {(record.actionName == '登录' && record?.result) && (<>
                  <Descriptions.Item label="认证器类型" span={3} >{record?.user?.relatedAuthType || record?.user?.uauthType || '-'}</Descriptions.Item>
                  <Descriptions.Item label="关联认证" span={3} >{record?.user?.relatedAuthType ? '是' : '否'}</Descriptions.Item></>)}
              </Descriptions>
            </>
          },
          rowExpandable: (record) => true,
          expandRowByClick: true,
          expandedRowKeys: expandedRowKeys,
          onExpand(expanded, record) {
            // console.log(expanded, record)
            if (!expanded) {
              setExpandedRowKeys([])
              return;
            }
            setExpandedRowKeys([record.id])
          },
        }}
        request={async (params = {}, sort, filter) => {
          console.log(params, sort, filter);
          const response = request<{
            data: any[];
          }>(`/api/audit/user`, {
            params: {
              ...params,
              //orgId: selectedKeys[0]
            }
          }).then((res) => {
            return {
              data: res.data?.list || [],
              total: res.data.total,
              success: true,
              pageSize: res.data.pageSize,
              current: res.data.current
            };
          })
          console.log(response);
          return Promise.resolve(response);
        }
        }
        // editable={{
        //   type: 'multiple',
        // }}
        // columnsState={{
        //   persistenceKey: 'pro-table-singe-demos',
        //   persistenceType: 'localStorage',
        //   onChange(value) {
        //     console.log('value: ', value);
        //   },
        // }}
        rowKey="id"
      // options={{
      //   setting: {
      //     listsHeight: 400,
      //   },
      // }}
      // form={{
      //   // 由于配置了 transform，创建的参与与定义的不同这里需要转化一下
      //   syncToUrl: (values, type) => {
      //     if (type === 'get') {
      //       return {
      //         ...values,
      //         created_at: [values.startTime, values.endTime],
      //       };
      //     }
      //     return values;
      //   },
      // }}
      // dateFormatter="string"
      // headerTitle="高级表格"
      />
    </PageContainer>
  );
};

export default CardList;
