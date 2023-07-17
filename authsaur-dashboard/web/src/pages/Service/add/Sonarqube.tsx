import { beforeUpload, getBase64, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Button, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { saveOrUpdateSonarqube } from './service';

const Page: React.FC = () => {
  const formRef = useRef<FormInstance>();
  const type = { type: 'Sonarqube' };

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
            const response = await saveOrUpdateSonarqube({
              ...values, type: type.type, logo: imageUrl, appConfig: {
                url: values?.url,
                applicationId: values?.applicationId,
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
                  initialValue={'Sonarqube'}
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
                <ProFormText
                  label="Application ID"
                  allowClear={false}
                  name="applicationId"
                  rules={[{ required: true, message: '请输入ID' }]}
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
