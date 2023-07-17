import { beforeUpload, getBase64, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import ProForm, { ProFormItem, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Card, Col, FormInstance, Row, Space, notification } from 'antd';
import Upload, { RcFile, UploadChangeParam, UploadFile, UploadProps } from 'antd/lib/upload';
import React, { useRef, useState } from 'react';
import { Link, history } from 'umi';
import { saveOrUpdateJumpServer } from './service';

const Page: React.FC = () => {
  const formRef = useRef<FormInstance>();
  const type = { type: 'JumpServer' };

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
            const response = await saveOrUpdateJumpServer({
              ...values, type: type.type, logo: imageUrl, appConfig: {
                url: values?.url,
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
                  initialValue={'JumpServer'}
                  allowClear={false}
                  rules={[{ required: true, message: '请输入收款人姓名' }]}
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
