import { beforeUpload, getBase64, getServiceType, marketServices } from '@/tools';
import { ArrowLeftOutlined, LoadingOutlined, PlusOutlined, SwapRightOutlined } from '@ant-design/icons';
import { ProCard, ProForm, ProFormSelect } from '@ant-design/pro-components';
import { ProFormItem, ProFormText } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import { Alert, Breadcrumb, Button, Card, Col, FormInstance, notification, Popconfirm, Row, Space, Upload } from 'antd';
import type { UploadChangeParam } from 'antd/es/upload';
import type { RcFile, UploadFile, UploadProps } from 'antd/es/upload/interface';
import React, { useRef, useState } from 'react';
import { history, Link } from 'umi';
import { queryFakeItem, removeFakeItem, saveOrUpdateAuthsaurAdmin, fakeSubmitAttributeRelease } from '../add/service';
import type { StepDataType } from './data';


const Page: React.FC = (props) => {
  const [stepData, setStepData] = useState<StepDataType>({ type: 'authsaur-admin' });
  const [pageLoading, setPageLoading] = useState(false);

  const [tab, setTab] = useState('tab1');

  const formRef = useRef<FormInstance>();
  const formRef2 = useRef<FormInstance>();

  const [loading, setLoading] = useState(false);
  const [imageUrl, setImageUrl] = useState<string>();

  const id = props.match.params;
  React.useEffect(() => {
    setPageLoading(true)
    queryFakeItem(id).then(function (resp) {
      console.log(resp);
      setPageLoading(false)
      const response = resp?.data || {};
      const type = response?.type || getServiceType(response)
      setStepData({
        ...response,
        type: type,
      });
      formRef.current?.setFieldsValue({
        ...response,
        type: type,
        url: response?.appConfig?.url,
      });
      formRef2.current?.setFieldsValue({
        id: id.id,
        principalAttr: response?.attributeRelease?.principalAttr || '',
        ...response?.attributeRelease?.renamedAttributes,
      });
      setImageUrl(response?.logo)

    });
  }, [id]);

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
      <Link to="/service/list">
        <Space>
          <ArrowLeftOutlined />
          <span>返回</span>
        </Space>
      </Link>
    </Breadcrumb>}>
      <ProCard
        loading={pageLoading}
        bordered={false}
        title={<Card bordered={false} bodyStyle={{ paddingLeft: '0' }}>
          <Card.Meta
            avatar={marketServices.get(stepData.type)?.avatar}
            title={marketServices.get(stepData.type)?.title}
            description={marketServices.get(stepData.type)?.description}
          />
        </Card>}
        tabs={{
          activeKey: tab,
          style: { paddingLeft: '8px' },
          items: [
            {
              label: `基本信息`,
              key: 'tab1',
              forceRender: true,
              children: <>
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
                    const response = await saveOrUpdateAuthsaurAdmin({
                      ...values, logo: imageUrl, appConfig: {
                        url: values?.url,
                      }
                    });
                    if (response.success == true) {
                      notification.success({ message: '更新成功' });
                      history.push('/service/list');
                      return true;
                    } else {
                      notification.error({ message: '更新失败:' + response?.msg });
                      return false;
                    }
                  }}
                >
                  <Card
                    bordered={false}
                    bodyStyle={{ padding: '24px 0 0px' }}
                    headStyle={{ padding: '0px 0 0px' }}
                    title={<>基本配置</>}
                  >
                    <Row gutter={48}>
                      <Col xs={24} lg={12}>
                        <ProFormText name="id" hidden disabled />
                        <ProFormText label="应用类型" name="type" hidden disabled />
                        <ProFormText
                          label="应用名称"
                          name="name"
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
                      </Col></Row>
                    </Card>
                  </Card>
                </ProForm>
                <Alert
                  showIcon
                  style={{ marginTop: '32px', padding: '24px' }}
                  message={
                    <>危险操作区域：此操作不可恢复，请谨慎操作！
                      <Popconfirm
                        title="确认删除此应用吗？"
                        onConfirm={() => {
                          removeFakeItem(id).then(function (response) {
                            if (response.success == true) {
                              notification.success({ message: '删除成功' });
                              history.push('/service/list');
                              return true;
                            } else {
                              notification.error({ message: '删除失败:' + response.data.message });
                              return false;
                            }
                          });
                        }}
                        okText="删除"
                        cancelText="取消"
                      >

                        <Button
                          type="primary"
                          danger
                          key="goToTree1"
                        >
                          删除应用
                        </Button>
                      </Popconfirm>
                    </>
                  }
                  type="warning" />

              </>
            },
            {
              label: `属性发布`,
              key: 'tab2',
              forceRender: true,
              children: <Card loading={pageLoading} bordered={false} bodyStyle={{ padding: '0' }}>
                <ProForm
                  // layout='horizontal'
                  // labelCol={{ span: 3 }}
                  // wrapperCol={{ span: 18 }}
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
                  formRef={formRef2}
                  onFinish={async (values) => {
                    const response = await fakeSubmitAttributeRelease({ ...values });
                    if (response.success == true) {
                      notification.success({ message: '更新成功' });
                      return true;
                    } else {
                      notification.error({ message: '更新失败:' + response?.msg });
                      return false;
                    }
                  }}
                >
                  <ProFormText name="id" hidden disabled />
                  <Card
                    bordered={false}
                    bodyStyle={{ padding: '24px 0 0px' }}
                    headStyle={{ padding: '0px 0 0px' }}
                    title={<>主键字段</>}
                  >
                    <ProFormSelect
                      label="选择用户标识字段"
                      width="md"
                      valueEnum={{
                        '': '用户ID',
                        'umail': '邮箱',
                        'uphone': '手机号',
                      }}
                      initialValue={''}
                      allowClear={false}
                      name="principalAttr"
                    />
                  </Card>
                  <Card
                    bordered={false}
                    bodyStyle={{ padding: '24px 0 0px' }}
                    headStyle={{ padding: '0px 0 0px' }}
                    title={<>映射策略</>}
                  >

                    <ProFormText
                      width='md'
                      // label="邮箱"
                      name="umail"
                      fieldProps={{
                        addonBefore: <span style={{ width: 150, display: 'inline-block' }}>邮箱umail<SwapRightOutlined /></span>
                      }}
                      placeholder="新字段名"
                      allowClear={false}
                    />
                    <ProFormText
                      width='md'
                      // label="手机"
                      name="uphone"
                      fieldProps={{
                        addonBefore: <span style={{ width: 150, display: 'inline-block' }}>手机uphone<SwapRightOutlined /></span>
                      }}
                      placeholder="新字段名"
                      allowClear={false}
                    />
                    <ProFormText
                      width='md'
                      // label="姓名"
                      name="uname"
                      fieldProps={{
                        addonBefore: <span style={{ width: 150, display: 'inline-block' }}>姓名uname<SwapRightOutlined /></span>
                      }}
                      placeholder="新字段名"
                      allowClear={false}
                    />

                  </Card>

                </ProForm>
              </Card>,
            },
          ],
          onChange: (key) => {
            setTab(key);
          },
        }}
      >
      </ProCard>
    </PageContainer >
  );
};

export default Page;
