import UserSearch from '@/pages/Account/UserSearch';
import { getAuthnHandler } from '@/tools';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { ProForm, ProFormCheckbox, ProFormItem, ProFormSwitch, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Card, FormInstance, Space, notification } from 'antd';
import qs from 'qs';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { fakeSubmitFormOTP, queryFakeList, queryFakeUserList } from '../add/service';


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

const StepForm: React.FC<Record<string, any>> = () => {
  const formRef = useRef<FormInstance>();
  const type = "mfa_otp";
  const [selectValue, setSelectValue] = useState();
  return (
    <PageContainer content={<Breadcrumb>
      <Link to="/authentication/method">
        <Space>
          <ArrowLeftOutlined />
          <span>返回</span>
        </Space>
      </Link>
    </Breadcrumb>}>
      {!getAuthnHandler(type) ? <></> : <Card bordered={false}
        title={<Card bordered={false} bodyStyle={{ paddingLeft: '0' }}>
          <Card.Meta
            avatar={getAuthnHandler(type)?.icon}
            title={getAuthnHandler(type)?.label}
            description={getAuthnHandler(type)?.desc}
          />
        </Card>}>
        {/* <StepsForm
          containerStyle={{
            margin: 'unset',
            marginTop: '-40px',
            width: '100%'
          }}
          stepsRender={() => false}
          current={current}
          formRef={formRef}
          onCurrentChange={setCurrent}
        > */}

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
          title="填写OTP认证信息"
          formRef={formRef}
          onFinish={async (values) => {
            const bypassUserIds = [];
            selectValue?.forEach((item) => {
              bypassUserIds.push(item.value);
            })
            const params = { id: values?.id, state: values?.state, name: values?.name, properties: { ...values, bypassUserIds } };
            const response = await fakeSubmitFormOTP(params);
            if (response.success == true) {
              notification.success({ message: '添加成功' });
              history.push('/authentication/list');
              return true;
            } else {
              notification.error({ message: '添加失败:' + response?.msg || '' });
              return false;
            }
          }}
        >
          <ProFormText width="xl" name="state" hidden disabled initialValue={"ACTIVE"} />
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
              initialValue={'Authsaur'}
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

          </Card>
        </ProForm>
      </Card>}
    </PageContainer>
  );
};

export default StepForm;
