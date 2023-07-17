import { beforeUpload, getBase64, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Button, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { saveOrUpdateJira } from './service';

const Page: React.FC = () => {
  const formRef = useRef<FormInstance>();
  const type = { type: 'Jira' };

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
            const response = await saveOrUpdateJira({
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
                  initialValue={'Jira'}
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
