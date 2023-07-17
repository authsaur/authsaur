import { beforeUpload, getBase64, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Button, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { saveOrUpdateConfluence } from './service';

const Page: React.FC = () => {
  const formRef = useRef<FormInstance>();
  const type = { type: 'Confluence' };

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
            const response = await saveOrUpdateConfluence({
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
                  initialValue={'Confluence'}
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
