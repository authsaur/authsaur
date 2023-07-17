import { beforeUpload, getBase64, standardServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormSwitch, ProFormText, ProFormTextArea } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { fakeSubmitForm } from './service';

const Page: React.FC = (props) => {
  const formRef = useRef<FormInstance>();
  const type = props.match.params;

  const [loading, setLoading] = useState(false);
  const [imageUrl, setImageUrl] = useState<string>();

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
      <Link to="/service/protocol">
        <Space>
          <ArrowLeftOutlined />
          <span>返回</span>
        </Space>
      </Link>
    </Breadcrumb>}>
      {!standardServices.get(type.type) ? <></> : <Card bordered={false}
        title={<Card bordered={false} bodyStyle={{ paddingLeft: '0' }}>
          <Card.Meta
            avatar={standardServices.get(type.type)?.avatar}
            title={standardServices.get(type.type)?.title}
            description={standardServices.get(type.type)?.description}
          />
        </Card>}>
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
            const response = await fakeSubmitForm({ ...values, type: type.type, logo: imageUrl });
            if (response.success == true) {
              notification.success({ message: '添加成功' });
              history.push("/service/list");
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
            title={<>基础配置</>}

          // extra={<></>}
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
                {type.type == 'SAML' && (
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
                {type.type == 'OAuth2' && (
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
                {type.type == 'Oidc' && (
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
                {type.type == 'CAS' && (
                  <>
                    <ProFormText
                      label="后端单点登出地址"
                      name="logoutUrl"
                      allowClear={false}
                    />

                  </>
                )}
                {type.type == 'zentao' && (
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
                {type.type == 'SAML' && (
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
      </Card>}
    </PageContainer>
  );
};

export default Page;
