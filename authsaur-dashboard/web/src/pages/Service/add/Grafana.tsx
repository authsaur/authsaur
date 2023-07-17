import { beforeUpload, getBase64, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { saveOrUpdateGrafana } from './service';

const Page: React.FC = () => {
  const formRef = useRef<FormInstance>();
  const type = { type: 'Grafana' };

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
          // title="填写应用信息"
          formRef={formRef}
          onFinish={async (values) => {
            const response = await saveOrUpdateGrafana({
              ...values, type: type.type, logo: imageUrl, appConfig: {
                url: values?.url,
                clientId: values?.clientId,
                clientSecret: values?.clientSecret,
              }
            });
            if (response.success == true) {
              notification.success({ message: '添加成功' });
              history.push('/service/list');
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
                  initialValue={'Grafana'}
                  allowClear={false}
                  rules={[{ required: true, message: '请输入收款人姓名' }]}
                />
                <ProFormText
                  label="访问地址"
                  allowClear={false}
                  name="url"
                  rules={[{ required: true, message: '请输入地址' }]}
                />
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
