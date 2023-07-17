import { ProFormSwitch, ProFormText, ProFormTextArea, StepsForm } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import type { FormInstance } from 'antd';
import { Avatar, Button, Card, notification } from 'antd';
import React, { useRef, useState } from 'react';
import { history } from 'umi';
import { fakeSubmitUpdateForm, queryFakeItem, removeFakeItem } from '../add/service';
import type { StepDataType } from './data';


const Page: React.FC = (props) => {
  const [stepData, setStepData] = useState<StepDataType>({
  });

  const [current, setCurrent] = useState(0);
  const formRef = useRef<FormInstance>();


  // const {
  //   data: listData,
  //   loading,
  // } = useRequest(() => {
  //   return queryFakeItem({
  //     count: 50,
  //   });
  // });
  const id = props.match.params;
  React.useEffect(() => {
    console.log(props)
    queryFakeItem(id).then(function (response) {
      console.log(response);
      let type = 'CAS';
      if (response['@class'].includes('Regex')) {
        type = 'CAS';
      } else if (response['@class'].includes('OAuth')) {
        type = 'OAuth2';
      } else if (response['@class'].includes('SAML')) {
        type = 'SAML';
      } else if (response['@class'].includes('Password')) {
        type = 'zentao';
      }
      setStepData({
        ...response,
        type: type
      })
      formRef.current?.setFieldsValue(response);
    })
  }, [props])



  // setStepData(listData || {});



  return (
    <PageContainer>
      <Card bordered={false}
        title={<>{stepData.payAccount}</>}
        extra={<Button type="primary" danger key="goToTree1" onClick={() => {
          removeFakeItem(id).then(function (response) {
            if (response.data.message == 'Ok') {
              notification.success({ message: '删除成功' });
              history.push("/service/list");
              return true;
            } else {
              notification.error({ message: '删除失败:' + response.data.message });
              return false;
            }
          })
        }}>
          删除应用
        </Button>}>
        <StepsForm
          current={current}
          onCurrentChange={setCurrent}
          stepsRender={(steps, dom) => { return <></> }}
          submitter={{
            render: (prop, dom) => {
              return [
                <Button type="primary" key="goToTree" onClick={() => prop.onSubmit?.()}>
                  更新
                </Button>

              ];
            },
          }}
        >
          <StepsForm.StepForm title="填写应用信息"
            // initialValues={stepData}
            formRef={formRef}
            onFinish={async (values) => {
              console.log(values)
              setStepData(values);
              const response = await fakeSubmitUpdateForm(values);
              console.log(response);
              if (response.data.message == 'Ok') {
                notification.success({ message: '更新成功' });
                history.push("/service/list");
                return true;
              } else {
                notification.error({ message: '更新失败:' + response.data.message });
                return false;
              }

            }}>
            <ProFormText
              width="xl"
              name="type"
              hidden
            />

            <Card bordered={false}
              bodyStyle={{ padding: '24px 0 0px' }}
              headStyle={{ padding: '0px 0 0px' }}
              title={<>{stepData.type}</>}
              extra={<Avatar src="https://joeschmoe.io/api/v1/random" />}>


              <ProFormText
                label="应用名称"
                width="xl"
                name="name"
                rules={[{ required: true, message: '请输入收款人姓名' }]}
                placeholder="请输入应用姓名"
              />
              <ProFormText
                label="应用地址"
                width="xl"
                name="serviceId"
                rules={[{ required: true, message: '请输入收款人姓名' }]}
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

        </StepsForm>

      </Card>
    </PageContainer>
  );
};

export default Page;
