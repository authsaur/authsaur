import { beforeUpload, getBase64, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormSwitch, ProFormText, ProFormTextArea } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { saveOrUpdateZentao } from './service';


const Page: React.FC = () => {
  const formRef = useRef<FormInstance>();
  const type = { type: 'zentao' };

  const [loading, setLoading] = useState(false);
  const [imageUrl, setImageUrl] = useState<string>('data:image/png;base64,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');

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
      <Link to="/service/method">
        <Space>
          <ArrowLeftOutlined />
          <span>返回</span>
        </Space>
      </Link>
    </Breadcrumb>}>
      {!marketServices.get(type.type) ? <></> : <Card bordered={false}
        title={<Card bordered={false} bodyStyle={{ paddingLeft: '0' }}>
          <Card.Meta
            avatar={marketServices.get(type.type)?.avatar}
            title={marketServices.get(type.type)?.title}
            description={marketServices.get(type.type)?.description}
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
            const response = await saveOrUpdateZentao({
              ...values, type: type.type, logo: imageUrl, appConfig: {
                clientId: values?.clientId,
                code: values?.code,
                secret: values?.secret,
              }
            });
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
                  initialValue={'禅道'}
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
          </Card>
        </ProForm>
      </Card>}
    </PageContainer>
  );
};

export default Page;
