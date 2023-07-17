import { ProFormSwitch, ProFormText, ProFormTextArea, StepsForm } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import ProList from '@ant-design/pro-list';
import type { FormInstance } from 'antd';
import { Avatar, Button, Card, Descriptions, Result, Statistic, notification } from 'antd';
import type { ReactText } from 'react';
import React, { useRef, useState } from 'react';
import { history } from 'umi';
import type { StepDataType } from './data.d';
import { fakeSubmitForm } from './service';
import styles from './style.less';


const dataSource = [
  {
    title: 'OAuth2',
    avatar: <Avatar size="large" src="/OAuth2.svg" />,
    description: 'OAuth 2.0 是行业标准的授权协议。',
  },
  {
    title: 'SAML',
    avatar: <Avatar size="large" src="/saml2.svg" />,
    description: 'SAML 是安全断言标记语言，是一个基于 XML 的开源标准数据格式。',
  },
  {
    title: 'CAS',
    avatar: <Avatar size="large" src="/cas.svg" />,
    description: 'CAS 是中央认证服务，一种独立开放指令协议。',
  },
  {
    title: 'zentao',
    avatar: <Avatar size="large" src="/zentao.svg" />,
    description: '免密认证。',
  },
];

const StepResult: React.FC<{
  onFinish: () => Promise<void>;
}> = (props) => {
  return (
    <Result
      status="success"
      title="创建成功"
      // subTitle="预计两小时内到账"
      extra={
        <>
          {/* <Button type="primary" onClick={props.onFinish}>
            再转一笔
          </Button> */}
          <Button onClick={(e) => history.push("/service/list")}>返回列表</Button>
        </>
      }
      className={styles.result}
    >
      {props.children}
    </Result>
  );
};

const StepForm: React.FC<Record<string, any>> = () => {
  const [stepData, setStepData] = useState<StepDataType>({
    payAccount: 'ant-design@alipay.com',
    receiverAccount: 'test@example.com',
    receiverName: 'Alex',
    amount: '500',
    receiverMode: 'alipay',
    type: 'OAuth2',
  });
  const [current, setCurrent] = useState(0);
  const formRef = useRef<FormInstance>();

  const [selectedRowKeys, setSelectedRowKeys] = useState<ReactText[]>(['OAuth2']);
  const rowSelection = {
    selectedRowKeys,
    type: 'radio',
    onChange: (keys: ReactText[]) => {
      setSelectedRowKeys(keys);
      console.log(keys)
      setStepData({
        "type": keys
      });
    },
  };

  return (
    <PageContainer>
      <Card bordered={false}>
        <StepsForm
          current={current}
          onCurrentChange={setCurrent}
          submitter={{
            render: (props, dom) => {
              if (props.step === 1) {
                return [
                  <Button key="gotoTwo" onClick={() => props.onPre?.()}>
                    上一步
                  </Button>,
                  <Button type="primary" key="goToTree" onClick={() => props.onSubmit?.()}>
                    创建
                  </Button>,
                ];
              }
              if (props.step === 2) {
                return null;
              }
              return dom;
            },
          }}
        >
          <StepsForm.StepForm<StepDataType>
            title="选择应用类型"
          // formRef={formRef}
          // initialValues={stepData}
          // onFinish={async (values) => {
          //   setStepData(values);
          //   return true;
          // }}
          >
            <ProList<{ title: string }>
              className="prolist"
              metas={{
                title: {},
                // description: {
                //   render: () => {
                //     return 'Ant Design, a design language for background applications, is refined by Ant UED Team';
                //   },
                // },
                description: {},
                avatar: {},
              }}
              tableAlertRender={false}
              rowKey="title"
              rowSelection={rowSelection}
              dataSource={dataSource}
            />
          </StepsForm.StepForm>

          <StepsForm.StepForm title="填写应用信息"
            initialValues={stepData}
            onFinish={async (values) => {
              console.log(values)
              setStepData(values);
              const response = await fakeSubmitForm(values);
              console.log(response);
              if (response.id) {
                notification.success({ message: '添加成功' });
                return true;
              } else {
                notification.error({ message: '添加失败:' + response.data.message });
                return false;
              }
              
            }}>
            <ProFormText
              width="xl"
              name="type"
              hidden
              placeholder="请输入应用"
            />

            <Card bordered={false}
              bodyStyle={{ padding: '24px 0 0px' }}
              headStyle={{ padding: '0px 0 0px' }}
              title={<>配置{stepData.type}</>}
              extra={<Avatar src="https://joeschmoe.io/api/v1/random" />}>


              <ProFormText
                label="应用名称"
                width="xl"
                name="name"
                rules={[{ required: true, message: '请输入应用名称' }]}
                placeholder="请输入应用名称"
              />
              <ProFormText
                label="应用地址"
                width="xl"
                name="serviceId"
                rules={[{ required: true, message: '请输入应用地址' }]}
                placeholder="请输入应用地址正则表达式"
              />
              {
                stepData.type == 'SAML' &&
                <>
                  <ProFormText
                    label="元数据地址"
                    width="xl"
                    name="metadataLocation"
                  />
                  <ProFormSwitch
                    label="断言签名"
                    width="xl"
                    name="signAssertions"
                  />
                </>
              }
              {
                stepData.type == 'OAuth2' &&
                <>
                  <ProFormText
                    label="clientId"
                    width="xl"
                    name="clientId"
                  />
                  <ProFormText
                    label="clientSecret"
                    width="xl"
                    name="clientSecret"
                  />
                </>
              }
              {
                stepData.type == 'zentao' &&
                <>
                  <ProFormText
                    label="clientId"
                    width="xl"
                    name="clientId"
                  />
                </>
              }
              <ProFormTextArea
                label="描述"
                width="xl"
                name="description"
                rules={[{ required: false, message: '请输入收款人姓名' }]}
              />

            </Card>
          </StepsForm.StepForm>
          <StepsForm.StepForm title="完成">
            <StepResult
              onFinish={async () => {
                setCurrent(0);
                formRef.current?.resetFields();
              }}
            >
              {/* <StepDescriptions stepData={stepData} /> */}
            </StepResult>
          </StepsForm.StepForm>
        </StepsForm>
        {/* <Divider style={{ margin: '40px 0 24px' }} />
        <div className={styles.desc}>
          <h3>说明</h3>
          <h4>转账到支付宝账户</h4>
          <p>
            如果需要，这里可以放一些关于产品的常见问题说明。如果需要，这里可以放一些关于产品的常见问题说明。如果需要，这里可以放一些关于产品的常见问题说明。
          </p>
          <h4>转账到银行卡</h4>
          <p>
            如果需要，这里可以放一些关于产品的常见问题说明。如果需要，这里可以放一些关于产品的常见问题说明。如果需要，这里可以放一些关于产品的常见问题说明。
          </p>
        </div> */}
      </Card>
    </PageContainer>
  );
};

export default StepForm;
