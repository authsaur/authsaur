import { getAuthnHandler } from '@/tools';
import { PlusOutlined } from '@ant-design/icons';
import { ProColumns, ProTable } from '@ant-design/pro-components';
import { PageContainer } from '@ant-design/pro-layout';
import { Button, Modal, Space, Typography } from 'antd';
import { Link, history, useRequest } from 'umi';
import AuthSwitch from '../AuthSwitch';
import { queryFakeList } from './service';
import styles from './style.less';

const info = () => {
  Modal.info({
    title: '认证器：密码登录',
    content: (
      <div>
        <p>密码登录是内置认证器，使用本地身份源内由用户或管理员选择的密码验证身份。</p>
      </div>
    ),
    // onOk() { },
    okText:'关闭'
  });
};


const columns: ProColumns<any>[] = [
  // {
  //   dataIndex: 'index',
  //   valueType: 'indexBorder',
  //   width: 48,
  //   align:'center'
  // },
  {
    dataIndex: 'name',
    title: '名称',
    render: (dom, record) => {
      if (record.type == 'pass_word') {
        return <a onClick={() => info()} ><Typography.Text strong> {record.name}</Typography.Text></a>
      }
      return <Link to={`/authentication/edit/${record.id}/${record.type}`} >
        <Typography.Text strong> {record.name}</Typography.Text>
      </Link >;
    },
  },
  {
    dataIndex: 'type',
    title: '方式',
    render: (dom, record) => {
      return <Space>{getAuthnHandler(record.type).smallIcon}
        {getAuthnHandler(record.type).label}
      </Space>;
    },
  },
  {
    dataIndex: 'category',
    title: '类型',
    render: (dom, record) => {
      if (record.type.startsWith('mfa_')) {
        return "双因素认证";
      }
      if (record.type.startsWith('social_')) {
        return "社会化认证";
      }
      return "基础认证";
    },
  },
  {
    dataIndex: 'state',

    title: '状态',
    render: (text, record, _) => {
      return <AuthSwitch source={record} />
    }
  },
];


const CardList = () => {

  const { data, loading } = useRequest(() => {
    return queryFakeList({
      count: 5,
    });
  });

  const list = data?.list || [];


  return (
    <PageContainer>
      <div className={styles.authn}>
        <ProTable
          // bordered
          // title={() => '认证器'}
          loading={loading}
          search={false}
          options={{
            reload: false,
            density: false,
            setting: false,
            // fullScreen: true,
          }}
          size="large"
          pagination={false}
          columns={columns}
          dataSource={list}
          rowKey="id"
          toolBarRender={() => [
            <Button
              key="button" icon={<PlusOutlined />} type="primary" onClick={() => history.push('/authentication/method')}>
              创建认证器
            </Button>,
          ]}
        />
      </div>
    </PageContainer>
  );
};

export default CardList;
