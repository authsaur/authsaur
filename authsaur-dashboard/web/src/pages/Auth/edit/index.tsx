import { getAuthnHandler } from '@/tools';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { ProFormInstance, ProFormSelect, ProFormSwitch, ProFormText, StepsForm } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Alert, Breadcrumb, Button, Card, Col, Popconfirm, Row, Space, notification } from 'antd';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { fakeSubmitUpdateForm, queryFakeItem, queryFakeList, removeFakeItem } from '../add/service';

const Page: React.FC = (props) => {
  const [stepData, setStepData] = useState<any>();
  const type = 'ldap';
  const [current, setCurrent] = useState(0);
  const formMapRef = useRef<React.MutableRefObject<ProFormInstance<any> | undefined>[]>([]);

  const id = props.match.params;
  React.useEffect(() => {
    console.log(props);
    queryFakeItem(id).then(function (resp) {
      console.log(resp);
      const response = resp?.data || {};
      setStepData({
        ...response,
        ...response.config
      });
      formMapRef?.current?.forEach((formInstanceRef) => {
        formInstanceRef?.current?.setFieldsValue({
          ...response,
          ...response.config
        });
      });
    });
  }, [id]);

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
        <StepsForm
          containerStyle={{
            margin: 'unset',
            // marginTop: '-40px',
            width: '100%'
          }}
          current={current}
          formMapRef={formMapRef}
          onCurrentChange={setCurrent}
          // stepsRender={(steps, dom) =>
          //   <></>
          // }
        >
          <StepsForm.StepForm
            title="填写LDAP服务器信息"
            onFinish={async (values) => {
              setStepData({ id: values?.id, state: values?.state, name: values?.name, properties: values })
              return true;
            }}
          >
            <ProFormText width="xl" name="id" hidden disabled />
            <ProFormText width="xl" name="state" hidden disabled />
            <Card
              bordered={false}
              bodyStyle={{ padding: '24px 0 0px' }}
              headStyle={{ padding: '0px 0 0px' }}
              title={<>基础配置</>}
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
              const response = await fakeSubmitUpdateForm({ ...values, ...stepData });
              if (response.success == true) {
                notification.success({ message: '更新成功' });
                history.push('/authentication/list');
                return true;
              } else {
                notification.error({ message: '更新失败:' + response?.msg });
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
                          // const transformData = [{ label: '本地认证', value: '-1024' },];
                          const transformData = [];
                          (res.data?.list || []).forEach((item) => {
                            if (item.id != id?.id && (item.relatedAuthnId == null || item.relatedAuthnId == undefined) && (item.type == 'ldap' || item.type == 'pass_word')) {
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
    </PageContainer>
  );
};

export default Page;
