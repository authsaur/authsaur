import { getAuthnHandler } from '@/tools';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { ProFormSelect, ProFormSwitch, ProFormText, StepsForm } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Alert, Breadcrumb, Button, Card, Col, FormInstance, notification, Row, Space } from 'antd';
import React, { useRef, useState } from 'react';
import { history, Link } from 'umi';
import { queryFakeList } from '../add/service';
import { fakeSubmitForm } from './service';

const StepForm: React.FC<Record<string, any>> = () => {
  const [current, setCurrent] = useState(0);
  const formRef = useRef<FormInstance>();
  const type = 'ldap';
  const [stepData, setStepData] = useState<any>();

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

        <StepsForm
          containerStyle={{
            margin: 'unset',
            // marginTop: '-20px',
            width: '100%'
          }}
          stepsProps={{
            type: "navigation"
          }}
          current={current}
          formRef={formRef}
          onCurrentChange={setCurrent}
          submitter={{
            render: (props, dom) => {
              if (props.step === 0) {
                return [
                  <Button onClick={(e) => history.push('/authentication/list')}>
                    返回
                  </Button>,
                  <Button type="primary" key="goToTree" onClick={() => props.onSubmit?.()}>
                    下一步
                  </Button>,
                ];
              }
              if (props.step === 1) {
                return [
                  <Button key="goToTree2" onClick={() => props.onPre?.()}>
                    上一步
                  </Button>,
                  <Button type="primary" key="goToTree5" onClick={() => props.onSubmit?.()}>
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

          <StepsForm.StepForm
            title="填写LDAP服务器信息"
            onFinish={async (values) => {
              setStepData({
                id: values?.id, state: values?.state, name: values?.name, properties: {
                  ...values,
                  principalAttributeList: values.principalAttributeList.split(",")
                }
              })
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
              <Row gutter={48}>
                <Col xs={24} lg={12}>
                  <ProFormText
                    label="名称"
                    allowClear={false}
                    name="name"
                    rules={[{ required: true, message: '请输入名称' }]}
                    placeholder="请输入名称"
                  /> </Col></Row>
              <Row gutter={48}>
                <Col xs={24} lg={12}>
                  <ProFormText
                    label="LDAP 链接"
                    allowClear={false}
                    name="ldapUrl"
                    rules={[{ required: true, message: '请输入地址' }]}
                    placeholder="请输入地址"
                  />
                </Col>
                <Col xs={24} lg={12}>
                  <ProFormText
                    label="Bind DN"
                    allowClear={false}
                    name="bindDn"
                    rules={[{ required: true, message: '请输入' }]}
                  />
                </Col></Row>
              <Row gutter={48}>
                <Col xs={24} lg={12}>
                  <ProFormText
                    label="Bind DN 密码"
                    allowClear={false}
                    name="bindCredential"
                    rules={[{ required: true, message: '请输入' }]}
                  />
                </Col>
                <Col xs={24} lg={12}>
                  <ProFormText
                    label="Users DN"
                    allowClear={false}
                    name="baseDn"
                    rules={[{ required: true, message: '请输入' }]}
                  />
                </Col></Row>
              <ProFormSwitch
                label="开启StartTLS"

                name="useStartTls"
                initialValue={0}
              />
              <Card
                bordered={false}
                bodyStyle={{ padding: '24px 0 0px' }}
                headStyle={{ padding: '0px 0 0px' }}
                title={<>用户字段配置</>}
              >
                <Row gutter={48}>
                  <Col xs={24} lg={12}>
                    <ProFormText
                      label="字段列表"
                      allowClear={false}
                      name="principalAttributeList"
                      rules={[{ required: true, message: '请输入' }]}
                    />
                  </Col>
                  <Col xs={24} lg={12}>
                    <ProFormText
                      label="用户名认证字段"
                      allowClear={false}
                      name="principalAttributeId"
                      rules={[{ required: true, message: '请输入' }]}
                    />
                  </Col></Row>
                <Row gutter={48}>
                  <Col xs={24} lg={12}>
                    <ProFormText
                      label="查询过滤器模版（匹配字段使用%s占位符）"
                      allowClear={false}
                      name="searchFilterTemplate"
                      placeholder={"(&(objectClass=top)(%s={%s}))"}
                      rules={[{ required: true, message: '请输入' }]}
                    />
                  </Col></Row>
                <Row gutter={48}>
                  <Col xs={24} lg={12}>
                    <ProFormText
                      label="姓名字段"
                      allowClear={false}
                      name="nameAttribute"
                    />
                  </Col></Row>
                <Row gutter={48}>
                  <Col xs={24} lg={12}>
                    <ProFormText
                      label="邮箱字段"
                      allowClear={false}
                      name="emailAttribute"
                    /></Col></Row>
                <Row gutter={48}>
                  <Col xs={24} lg={12}>
                    <ProFormText
                      label="手机号字段"
                      allowClear={false}
                      name="phoneAttribute"
                    />
                  </Col></Row>
              </Card>
            </Card>
          </StepsForm.StepForm>
          <StepsForm.StepForm
            title="关联认证器身份"
            onFinish={async (values) => {
              const response = await fakeSubmitForm({ ...values, ...stepData });
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
            <Card
              bordered={false}
              bodyStyle={{ padding: '24px 0 0px' }}
              headStyle={{ padding: '0px 0 0px' }}
              title="关联认证器身份"
            >

              <Row gutter={48}>
                <Col xs={24} lg={12}>
                  <Alert
                    showIcon
                    style={{ marginBottom: '16px', padding: '24px' }}
                    message={
                      <>可关联「密码登录」或者「LDAP」认证器身份，当前认证身份源用户不会被使用。
                      </>
                    }
                    type="info" />
                  <ProFormSelect
                    name="relatedAuthnId"
                    label="认证器"
                    request={() => (
                      queryFakeList({ count: 5 })
                        .then((res) => {
                          const transformData = [];
                          (res.data?.list || []).forEach((item) => {
                            if ((item.relatedAuthnId == null || item.relatedAuthnId == undefined) && (item.type == 'ldap' || item.type == 'pass_word')) {
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
                </Col>
              </Row>
              <ProFormSwitch
                label="邮箱匹配"
                name="relatedWithMail"
              />
              <ProFormSwitch
                label="手机号匹配"
                name="relatedWithPhone"
              />
            </Card>
          </StepsForm.StepForm>
        </StepsForm>
      </Card>
      }
    </PageContainer>
  );
};

export default StepForm;
