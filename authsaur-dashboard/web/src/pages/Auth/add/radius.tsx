import { getAuthnHandler } from '@/tools';
import { ArrowLeftOutlined } from '@ant-design/icons';
import ProForm, { ProFormDigit, ProFormSelect, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Card, FormInstance, Space, notification } from 'antd';
import React, { useRef } from 'react';
import { Link, history } from 'umi';
import { fakeSubmitFormRadius } from './service';

const StepForm: React.FC<Record<string, any>> = () => {
  const formRef = useRef<FormInstance>();
  const type = 'radius';

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
          title="填写Radius服务器信息"
          formRef={formRef}
          onFinish={async (values) => {
            const response = await fakeSubmitFormRadius({ id: values?.id, state: values?.state, name: values?.name, properties: values });
            if (response.success == true) {
              notification.success({ message: '添加成功' });
              history.push('/authentication/list');
              return true;
            } else {
              notification.error({ message: '添加失败:' + response?.msg || '' });
              return false;
            }
            return true;
          }}
        >
          <ProFormText name="state" hidden disabled initialValue={"ACTIVE"} />
          <Card
            bordered={false}
            bodyStyle={{ padding: '24px 0 0px' }}
            headStyle={{ padding: '0px 0 0px' }}
            title={<>基础配置</>}

          // extra={<></>}
          >
            <ProFormText
              label="名称"
              width="xl"
              allowClear={false}
              name="name"
              rules={[{ required: true, message: '请输入名称' }]}
              placeholder="请输入名称"
            />
            <ProFormText
              label="Radius 地址"
              width="xl"
              allowClear={false}
              name="inetAddress"
              rules={[{ required: true, message: '请输入地址' }]}
              placeholder="请输入地址"
            />

            <ProFormText
              label="Shared Secret"
              width="xl"
              allowClear={false}
              name="sharedSecret"
              rules={[{ required: true, message: '请输入' }]}
            />
            <ProFormSelect
              label="服务协议"
              width="md"
              valueEnum={{
                PAP: 'PAP',
                CHAP: 'CHAP',
                EAP_MD5: 'EAP_MD5',
                EAP_MSCHAPv2: 'EAP_MSCHAPv2',
                PEAP: 'PEAP',
              }}
              initialValue={'PAP'}
              allowClear={false}
              name="protocol"
              rules={[{ required: true, message: '请输入' }]}
            />

            <ProFormDigit
              label="认证端口"
              width="md"
              allowClear={false}
              name="authenticationPort"
              initialValue={1812}
              fieldProps={{
                controls: false,
                precision: 0,
              }}
            />
            <ProFormDigit
              label="重试次数"
              width="sm"
              name="retries"
              initialValue={3}
              fieldProps={{
                addonAfter: '次',
                controls: false,
                precision: 0,
              }}
            />
            <ProFormDigit
              label="超时时间"
              width="sm"
              allowClear={false}
              name="socketTimeout"
              initialValue={3000}
              fieldProps={{
                addonAfter: '毫秒',
                controls: false,
                precision: 0,
              }}
            />

          </Card>
        </ProForm>
      </Card>
      }
    </PageContainer>
  );
};

export default StepForm;
