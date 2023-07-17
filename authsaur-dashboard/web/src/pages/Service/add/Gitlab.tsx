import { beforeUpload, getBase64, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormText, ProFormTextArea } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Button, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { saveOrUpdateGitlab } from './service';

const Page: React.FC = () => {
  const formRef = useRef<FormInstance>();
  const type = { type: 'Gitlab' };

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
            title={type.type}
            description={marketServices.get(type.type)?.description}
          />
        </Card>}
        extra={<Button
          key="button" type="default" disabled onClick={() => history.push('/service/method')}>
          接入教程
        </Button>}>
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
            const response = await saveOrUpdateGitlab({
              ...values, type: type.type, logo: imageUrl, appConfig: {
                url: values?.url,
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
                  initialValue={'Gitlab'}
                  allowClear={false}
                  rules={[{ required: true, message: '请输入收款人姓名' }]}
                  placeholder="请输入应用姓名"
                />
                <ProFormText
                  label="Gitlab地址"
                  allowClear={false}
                  name="url"
                  rules={[{ required: true, message: '请输入地址' }]}
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
              {/* <Col xs={24} lg={12}>
                  
                </Col> */}
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
                <ProFormTextArea
                  label="SAML AuthnRequest"

                  name="saml"
                  fieldProps={{ rows: 10 }}
                  placeholder="请输入SAML认证请求"
                />
              </Col></Row>
            </Card>
          </Card>
        </ProForm>
      </Card>}
    </PageContainer>
  );
};

export default Page;
