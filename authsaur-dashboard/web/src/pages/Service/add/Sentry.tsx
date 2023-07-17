import { beforeUpload, getBase64, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Button, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { saveOrUpdateSentry } from './service';

const Page: React.FC = () => {
  const formRef = useRef<FormInstance>();
  const type = { type: 'Sentry' };

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
            const response = await saveOrUpdateSentry({
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
                  initialValue={'Sentry'}
                  allowClear={false}
                  rules={[{ required: true, message: '请输入收款人姓名' }]}
                  placeholder="请输入应用姓名"
                />
                <ProFormText
                  label="访问地址"
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
          </Card>
        </ProForm>
      </Card>}
    </PageContainer>
  );
};

export default Page;
