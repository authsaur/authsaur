import UserSearch from '@/pages/Account/UserSearch';
import { getAuthnHandler } from '@/tools';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { ProForm } from '@ant-design/pro-components';
import { ProFormCheckbox, ProFormItem, ProFormSwitch, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Alert, Breadcrumb, Button, Card, FormInstance, notification, Popconfirm, Space } from 'antd';
import qs from 'qs';
import React, { useRef, useState } from 'react';
import { history, Link } from 'umi';
import { fakeSubmitFormOTP, queryFakeItem, queryFakeList, queryFakeUserList, removeFakeItem } from '../add/service';

let timeout: ReturnType<typeof setTimeout> | null;
let currentValue: string;

const fetch = (value: string, callback: Function) => {
  if (timeout) {
    clearTimeout(timeout);
    timeout = null;
  }
  currentValue = value;

  const fake = () => {
    const str = qs.stringify({
      code: 'utf-8',
      q: value,
    });
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

const Page: React.FC = (props) => {
  const type = "mfa_otp";
  const formRef = useRef<FormInstance>();
  const [selectValue, setSelectValue] = useState();

  const id = props.match.params;
  React.useEffect(() => {
    console.log(props);
    queryFakeItem(id).then(function (resp) {
      console.log(resp);
      const response = resp?.data || {};
      formRef?.current?.setFieldsValue({
        ...response,
        ...response.config,
      });
      const users = response?.config?.bypassUsers || [];
      setSelectValue(users.map((item) => ({ label: item.name + "(" + item.id + ")", value: item.id, key: item.id })));
    });
  }, [props]);

  

  return (
    <PageContainer content={<Breadcrumb>
      <Link to="/authentication/list">
        <Space>
          <ArrowLeftOutlined />
          <span>返回</span>
        </Space>
      </Link>
    </Breadcrumb>}>
      <Card bordered={false}
        title={<Card bordered={false} bodyStyle={{ paddingLeft: '0' }}>
          <Card.Meta
            avatar={getAuthnHandler(type)?.icon}
            title={getAuthnHandler(type)?.label}
            description={getAuthnHandler(type)?.desc}
          />
        </Card>}>

        <ProForm
          style={{
            marginTop: '-20px',
          }}
          submitter={{
            searchConfig: {
              submitText: '保存',
            },
            resetButtonProps: {
              style: {
                // 隐藏重置按钮
                display: 'none',
              },
            },
            submitButtonProps: {
              style: {
                marginLeft: '-8px',
              },
            },
          }}
          formRef={formRef}
          title="填写OTP认证信息"
          onFinish={async (values) => {
            const bypassUserIds = [];
            selectValue?.forEach((item) => {
              bypassUserIds.push(item.value);
            })
            const params = { id: values?.id, state: values?.state, name: values?.name, properties: { ...values, bypassUserIds } };
            const response = await fakeSubmitFormOTP(params);
            if (response.success == true) {
              notification.success({ message: '更新成功' });
              history.push('/authentication/list');
              return true;
            } else {
              notification.error({ message: '更新失败:' + response?.msg || '' });
              return false;
            }
          }}
        >
          <ProFormText width="xl" name="state" hidden disabled />
          <ProFormText width="xl" name="id" hidden disabled fieldProps={{ value: "mfa-gauth" }} />
          <Card
            bordered={false}
            bodyStyle={{ padding: '24px 0 0px' }}
            headStyle={{ padding: '0px 0 0px' }}
            title={<>基础配置</>}
          >
            <ProFormText
              label="名称"
              width="xl"
              name="name"
              rules={[{ required: true, message: '请输入名称' }]}
              placeholder="请输入名称"
            />
            <ProFormText
              label="签发者"
              width="xl"
              name="issuer"
              placeholder="请输入名称"
            />
            <ProFormText
              label="标签"
              width="xl"
              name="label"
              placeholder="请输入名称"
              initialValue={'HI'}
            />
            {/* <ProFormItem
                label="优先级">

                <InputNumber
                  min={0}
                  max={10}
                  defaultValue={0}
                />
              </ProFormItem> */}
          </Card>
          <Card
            bordered={false}
            bodyStyle={{ padding: '24px 0 0px' }}
            headStyle={{ padding: '0px 0 0px' }}
            title={<>策略配置</>}
          >
            <ProFormCheckbox.Group
              name="authIds"
              // layout="vertical"
              label="选择触发认证器"
              // options={['农业', '制造业', '互联网']}
              request={() => (
                queryFakeList({ count: 5 })
                  .then((res) => {
                    const transformData = [];
                    (res.data?.list || []).forEach((item) => {
                      if ((!item.type.startsWith("mfa_"))) {
                        transformData.push({
                          label: item.name || item.username || item.rule_name,
                          value: item.id,
                        });
                      }
                    });
                    return transformData;
                  })

              )}
            />
            <ProFormItem
              label="选择Bypass用户"
              name="bypassUserIds"
            >
              <UserSearch selectValue={selectValue} onChange={(v) => setSelectValue(v)} />
            </ProFormItem>
            <ProFormSwitch
              label="开启可信设备"
              width="xl"
              tooltip="是否应信任当前浏览器/设备，以便跳过后续的 MFA 请求"
              name="trustedDeviceEnabled"
              initialValue={1}
            />



          </Card>
        </ProForm>
        <Alert
          showIcon
          style={{ marginTop: '32px', padding: '24px' }}
          message={
            <>危险操作区域：此操作不可恢复，请谨慎操作！
              <Popconfirm
                title="确认删除此认证器吗？"
                onConfirm={() => {
                  removeFakeItem(id).then(function (response) {
                    if (response.success == true) {
                      notification.success({ message: '删除成功' });
                      history.push('/authentication/list');
                      return true;
                    } else {
                      notification.error({ message: '删除失败:' + response.data.message });
                      return false;
                    }
                  });
                }}
                okText="删除"
                cancelText="取消"
              >

                <Button
                  type="primary"
                  danger
                  key="goToTree1"
                >
                  删除认证器
                </Button>
              </Popconfirm>
            </>
          }
          type="warning" />
      </Card>
    </PageContainer >
  );
};

export default Page;
