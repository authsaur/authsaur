import { beforeUpload, getBase64, getServiceType, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import { ProCard, ProForm } from '@ant-design/pro-components';
import { ProFormItem, ProFormSwitch, ProFormText, ProFormTextArea, StepsForm } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Alert, Breadcrumb, Button, Card, Col, FormInstance, notification, Popconfirm, Row, Space, Upload } from 'antd';
import type { UploadChangeParam } from 'antd/es/upload';
import type { RcFile, UploadFile, UploadProps } from 'antd/es/upload/interface';
import React, { useRef, useState } from 'react';
import { history, Link } from 'umi';
import { queryFakeItem, removeFakeItem, saveOrUpdateZentao } from '../add/service';
import type { StepDataType } from './data';


const Page: React.FC = (props) => {
  const [stepData, setStepData] = useState<StepDataType>({ type: 'zentao' });
  const [pageLoading, setPageLoading] = useState(false);

  const formRef = useRef<FormInstance>();

  const [loading, setLoading] = useState(false);
  const [imageUrl, setImageUrl] = useState<string>();


  const id = props.match.params;
  React.useEffect(() => {
    setPageLoading(true)
    queryFakeItem(id).then(function (resp) {
      console.log(resp);
      setPageLoading(false)
      const response = resp?.data || {};
      const type = response?.type || getServiceType(response)
      setStepData({
        ...response,
        type: type,
      });
      formRef.current?.setFieldsValue({
        ...response,
        type: type,
        clientId: response?.appConfig?.clientId,
        code: response?.appConfig?.code,
        secret: response?.appConfig?.secret
      });
      setImageUrl(response?.logo)

    });
  }, [id]);



  const handleChange: UploadProps['onChange'] = (info: UploadChangeParam<UploadFile>) => {
    if (info.file.status === 'uploading') {
      setLoading(true);
      return;
    }
    if (info.file.status === 'done') {
      // Get this url from response in real world.
      getBase64(info.file.originFileObj as RcFile, (url) => {
        setLoading(false);
        setImageUrl(url);
      });
    }
  };

  const uploadButton = (
    <div>
      {loading ? <LoadingOutlined /> : <PlusOutlined />}
      {/* <div style={{ marginTop: 8 }}>Upload</div> */}
    </div>
  );


  return (
    <PageContainer content={<Breadcrumb>
      <Link to="/service/list">
        <Space>
          <ArrowLeftOutlined />
          <span>返回</span>
        </Space>
      </Link>
    </Breadcrumb>}>
      <ProCard
        loading={pageLoading}
        bordered={false}
        title={<Card bordered={false} bodyStyle={{ paddingLeft: '0' }}>
          <Card.Meta
            avatar={marketServices.get(stepData.type)?.avatar}
            title={marketServices.get(stepData.type)?.title}
            description={marketServices.get(stepData.type)?.description}
          />
        </Card>}
        tabs={{
          // activeKey: 'tab1',
          style: { paddingLeft: '8px' },
          items: [
            {
              label: `基本信息`,
              key: 'tab1',
              children: <>
                <ProForm
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
                  title="填写应用信息"
                  formRef={formRef}
                  onFinish={async (values) => {
                    const response = await saveOrUpdateZentao({
                      ...values, logo: imageUrl, appConfig: {
                        clientId: values?.clientId,
                        code: values?.code,
                        secret: values?.secret,
                      }
                    });
                    if (response.success == true) {
                      notification.success({ message: '更新成功' });
                      history.push('/service/list');
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
                    title={<>基本配置</>}
                  >
                    <Row gutter={48}>
                      <Col xs={24} lg={12}>
                        <ProFormText name="id" hidden disabled />
                        <ProFormText label="应用类型" name="type" hidden disabled />
                        <ProFormText
                          label="应用名称"
                          name="name"
                          allowClear={false}
                          rules={[{ required: true, message: '请输入收款人姓名' }]}
                          placeholder="请输入应用姓名"
                        />
                        <ProFormText
                          label="访问地址"
                          allowClear={false}
                          name="serviceId"
                          rules={[{ required: true, message: '请输入收款人姓名' }]}
                          placeholder="请输入访问地址正则表达式"
                        />
                      </Col>
                      <Col xs={24} lg={12}>
                        <ProFormItem
                          label="图标"
                          name="logo"
                        >
                          <Upload
                            name="avatar"
                            listType="picture-card"
                            className="avatar-uploader"
                            showUploadList={false}
                            customRequest={(option: any) => { option.onSuccess(); }}
                            beforeUpload={beforeUpload}
                            onChange={handleChange}
                          >
                            {imageUrl ? <img src={imageUrl} alt="avatar" style={{ width: '100%' }} /> : uploadButton}
                          </Upload>
                        </ProFormItem>
                      </Col>
                    </Row>
                    <Row gutter={48}>
                      <Col xs={24} lg={12}>
                        {stepData.type == 'SAML' && (
                          <>
                            {/* <ProFormText
                    label="ENTITY-ID"
                    
                    name="entityId"
                    rules={[{ required: true, message: '请输入' }]}
                  />
                  <ProFormText
                    label="断言消费地址"
                    
                    name="assertionConsumerServiceUrl"
                    rules={[{ required: true, message: '请输入' }]}
                  /> */}
                            <ProFormTextArea
                              label="元数据内容"
                              name="spMetadataContent"
                              fieldProps={{ rows: 10 }}
                              rules={[{ required: true, message: '请输入' }]}
                            />
                            <ProFormSwitch label="断言签名" name="signAssertions" />
                          </>
                        )}
                        {stepData.type == 'OAuth2' && (
                          <>
                            <ProFormText
                              label="CLIENT-ID"
                              allowClear={false}
                              name="clientId"
                              rules={[{ required: true, message: '请输入' }]}
                            />
                            <ProFormText
                              label="CLIENT-SECRET"
                              allowClear={false}
                              name="clientSecret"
                              rules={[{ required: true, message: '请输入' }]}
                            />
                            <ProFormSwitch label="显示授权同意页面" name="showApprovalPrompt" />
                          </>
                        )}
                        {stepData.type == 'Oidc' && (
                          <>
                            <ProFormText
                              label="CLIENT-ID"
                              allowClear={false}
                              name="clientId"
                              rules={[{ required: true, message: '请输入' }]}
                            />
                            <ProFormText
                              label="CLIENT-SECRET"
                              allowClear={false}
                              rules={[{ required: true, message: '请输入' }]}
                              name="clientSecret"
                            />
                            <ProFormSwitch label="显示授权同意页面" name="showApprovalPrompt" />
                          </>
                        )}
                        {stepData.type == 'CAS' && (
                          <>
                            <ProFormText
                              label="后端单点登出地址"
                              name="logoutUrl"
                              allowClear={false}
                            />

                          </>
                        )}
                        {stepData.type == 'zentao' && (
                          <>
                            <ProFormText
                              label="CLIENT-ID"
                              allowClear={false}

                              name="clientId"
                              rules={[{ required: true, message: '请输入' }]}
                            />
                            <ProFormText
                              label="代号"
                              allowClear={false}
                              name="code"
                              rules={[{ required: true, message: '请输入' }]}
                            />
                            <ProFormText
                              label="密钥"
                              allowClear={false}
                              name="secret"
                              rules={[{ required: true, message: '请输入' }]}
                            />
                          </>
                        )}
                      </Col>
                      <Col xs={24} lg={12}>
                        {/* <ProFormTextArea
                    label="描述"
                    name="description"
                    fieldProps={{ rows: 5 }}
                    rules={[{ required: false, message: '请输入收款人姓名' }]}
                  /> */}
                      </Col>
                    </Row>
                    <Card
                      bordered={false}
                      bodyStyle={{ padding: '24px 0 0px' }}
                      headStyle={{ padding: '0px 0 0px' }}
                      title={<>应用面板配置</>}
                    >

                      {/* <ProFormText
                  label="别名"
                  // tooltip={"12"}
                  width="sm"
                  name="alias"
                  placeholder=""
                /> */}
                      <Row gutter={48}><Col xs={24} lg={12}>
                        <ProFormText label="标签分类" name="tag" placeholder="" />
                        <ProFormText
                          label="点击跳转地址"
                          name="homePage"
                          allowClear={false}
                          placeholder="请输入应用图标跳转地址"
                        />
                        {stepData.type == 'SAML' && (
                          <>
                            <ProFormTextArea
                              label="SAML AuthnRequest"

                              name="saml"
                              fieldProps={{ rows: 10 }}
                              placeholder="请输入SAML认证请求"
                            />
                          </>
                        )}
                      </Col></Row>
                    </Card>
                  </Card>
                </ProForm>
                <Alert
                  showIcon
                  style={{ marginTop: '32px', padding: '24px' }}
                  message={
                    <>危险操作区域：此操作不可恢复，请谨慎操作！
                      <Popconfirm
                        title="确认删除此应用吗？"
                        onConfirm={() => {
                          removeFakeItem(id).then(function (response) {
                            if (response.success == true) {
                              notification.success({ message: '删除成功' });
                              history.push('/service/list');
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
                          删除应用
                        </Button>
                      </Popconfirm>
                    </>
                  }
                  type="warning" />

              </>
            }]
        }}
      >
      </ProCard>
    </PageContainer >
  );
};

export default Page;
