import { beforeUpload, getBase64, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Button, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { saveOrUpdateJenkins } from './service';

const Page: React.FC = () => {
  const formRef = useRef<FormInstance>();
  const type = { type: 'Jenkins' };

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
            const response = await saveOrUpdateJenkins({
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
                  initialValue={'Jenkins'}
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
