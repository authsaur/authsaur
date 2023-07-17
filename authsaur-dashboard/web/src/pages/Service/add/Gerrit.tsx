import { beforeUpload, getBase64, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { saveOrUpdateGerrit } from './service';

const Page: React.FC = () => {
  const formRef = useRef<FormInstance>();
  const type = { type: 'Gerrit' };

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
            const response = await saveOrUpdateGerrit({
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
                  initialValue={'Gerrit'}
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
