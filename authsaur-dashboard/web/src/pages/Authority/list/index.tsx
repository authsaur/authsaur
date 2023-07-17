import OrgSearch from '@/pages/Account/OrgSearch';
import UserSearch from '@/pages/Account/UserSearch';
import { coverString, getProtocolLogo, getServiceType } from '@/tools';
import { PlusOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import ProForm, { ProFormItem, ProFormRadio } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable, { ProColumns, TableDropdown } from '@ant-design/pro-table';
import {
  Avatar,
  Button,
  Col,
  Drawer,
  FormInstance,
  Input,
  Menu,
  MenuProps,
  Result,
  Row,
  Space,
  Tag,
  Tooltip,
  notification
} from 'antd';
import React, { useRef, useState } from 'react';
import { queryFakeList } from '../../Service/list/service';
import { fakeSubmitForm, queryFakeItem } from './service';
import styles from './style.less';

const CardList = () => {
  const [current, setCurrent] = useState();
  const [menuData, setMenuData] = useState();
  const [menuSource, setMenuSource] = useState();
  const [userLoading, setUserLoading] = useState<boolean>(false);
  const [orgLoading, setOrgLoading] = useState<boolean>(false);
  const [oprLoading, setOprLoading] = useState<boolean>(false);
  const [userData, setUserData] = useState<any[]>();
  const [orgData, setOrgData] = useState<any[]>();
  const formRef = useRef<FormInstance>();
  const formRefOrg = useRef<FormInstance>();

  const [open, setOpen] = useState(false);
  const [openOrg, setOpenOrg] = useState(false);


  const [selectValue, setSelectValue] = useState();
  const [selectValueOrg, setSelectValueOrg] = useState();

  const onClose = () => {
    setOpen(false);
  };
  const onSubmit = () => {
    formRef.current?.submit();
  };
  const showDrawer = () => {
    setOpen(true);
    setOprLoading(false);
    formRef.current?.resetFields();
    setSelectValue(undefined);
  };
  const onCloseOrg = () => {
    setOpenOrg(false);
  };
  const onSubmitOrg = () => {
    formRefOrg.current?.submit();
  };
  const showDrawerOrg = () => {
    setOpenOrg(true);
    setOprLoading(false);
    formRefOrg.current?.resetFields();
    setSelectValueOrg();
  };


  const userTable = () => {
    return (
      <>
        <ProTable
          loading={userLoading}
          pagination={false}
          headerTitle="用户授权"
          toolBarRender={() => [
            <Button key="button" icon={<PlusOutlined />} type="text" onClick={showDrawer}>
              添加
            </Button>,
          ]}
          search={false}
          options={false}
          dataSource={userData}
          columns={columns}
        />
      </>
    );
  };
  const orgTable = () => {
    return (
      <>
        <ProTable
          loading={orgLoading}
          pagination={false}
          headerTitle="部门授权"
          // bordered={false}
          toolBarRender={() => [
            <Button key="button" icon={<PlusOutlined />} type="text" onClick={showDrawerOrg}>
              添加
            </Button>,
          ]}
          search={false}
          options={false}
          dataSource={orgData}
          columns={orgColumns}
        />
      </>
    );
  };

  const onClick: MenuProps['onClick'] = (e) => {
    console.log('click ', e);
    if (e.key == current) return;
    setCurrent(e.key);
    setUserLoading(true);
    setOrgLoading(true);
    queryFakeItem({
      id: e.key,
    }).then(function (response) {
      setUserData(response?.data?.users || []);
      setOrgData(response?.data?.orgs || []);
      setUserLoading(false);
      setOrgLoading(false);
    });
  };

  const columns: ProColumns<any>[] = [
    {
      title: '用户',
      dataIndex: 'principal',
      // copyable: true,
      render: (text, record, _, action) => {
        const str = '(' + record.principal.substring(0, 6) + '...';
        return (
          <Tooltip title={record.principal}>
            {record.name}
            {str}
          </Tooltip>
        );
      },
    },
    {
      title: '授权作用',
      width: '200px',
      dataIndex: 'action',
      hideInSearch: true,
      render: (_, record) =>
        record.action == 'Allowed' ? <Tag color="green">允许</Tag> : <Tag color="red">拒绝</Tag>,
    },
    {
      // title: '操作',
      valueType: 'option',
      key: 'option',
      width: '160px',
      render: (text, record, _, action) => [
        <TableDropdown
          key="actionGroup"
          onSelect={(key) => {
            console.log(key, record);
            if (key == 'copy') {
            } else if (key == 'delete') {
              const userDatas = userData || [];
              const userValues = userDatas.filter((user) => user.principal != record.principal);
              fakeSubmitForm({
                appId: current,
                orgs: orgData || [],
                users: userValues,
              }).then((response) => {
                console.log(response);
                if (response.success == true) {
                  notification.success({ message: '删除成功' });
                  setUserLoading(true);
                  queryFakeItem({
                    id: current,
                  }).then(function (resp) {
                    setUserData(resp?.data?.users || []);
                    setUserLoading(false);
                  });
                  return true;
                } else {
                  notification.error({ message: '删除失败:' + response?.msg });
                  return false;
                }
              });
            }
          }}
          menus={[
            // { key: 'copy', name: '编辑' },
            { key: 'delete', name: '删除' },
          ]}
        ></TableDropdown>,
      ],
    },
  ];

  const orgColumns: ProColumns<any>[] = [
    {
      title: '部门',
      dataIndex: 'name',
      // copyable: true,
    },
    {
      title: '授权作用',
      dataIndex: 'action',
      width: '200px',
      hideInSearch: true,
      render: (_, record) => (record.action == 'Allowed' ? <Tag>允许</Tag> : <Tag>拒绝</Tag>),
    },
    {
      // title: '操作',
      valueType: 'option',
      key: 'option',
      width: '160px',
      render: (text, record, _, action) => [
        <TableDropdown
          key="actionGroup"
          onSelect={(key) => {
            console.log(key, record);
            if (key == 'copy') {
            } else if (key == 'delete') {
              const orgDatas = orgData || [];
              const orgValues = orgDatas.filter((org) => org.id != record.id);
              fakeSubmitForm({
                appId: current,
                orgs: orgValues || [],
                users: userData || [],
              }).then((response) => {
                console.log(response);
                if (response.success == true) {
                  notification.success({ message: '删除成功' });
                  setOrgLoading(true);
                  queryFakeItem({
                    id: current,
                  }).then(function (resp) {
                    setOrgData(resp?.data?.orgs || []);
                    setOrgLoading(false);
                  });
                  return true;
                } else {
                  notification.error({ message: '删除失败:' + response?.msg });
                  return false;
                }
              });
            }
          }}
          menus={[
            // { key: 'copy', name: '编辑' },
            { key: 'delete', name: '删除' },
          ]}
        ></TableDropdown>,
      ],
    },
  ];

  React.useEffect(() => {
    queryFakeList({
      count: 5,
    }).then(function (response) {
      const list = response?.data?.list || [];
      const menu = list.map((item) => {
        const type = item?.type || getServiceType(item);
        return {
          label: item.name,
          key: item.id,
          icon: <Avatar shape="square" size="small" src={item.logo || getProtocolLogo(type)} />,
        };
      });
      setMenuData(menu);
      setMenuSource(menu);
      setCurrent(menu[0]?.key + '');
      if (list.length > 0) {
        setUserLoading(true);
        setOrgLoading(true);
        queryFakeItem({
          id: menu[0]?.key,
        }).then(function (resp) {
          setUserData(resp?.data?.users || []);
          setOrgData(resp?.data?.orgs || []);
          setUserLoading(false);
          setOrgLoading(false);
        });
      }
    });
  }, []);

  const onSearch = (value: string) => {
    console.log(value);
    if (menuSource) {
      setMenuData(menuSource.filter((item) => coverString(value, item.label)));
    }
  };

  return (
    <PageContainer content=''>
      <Drawer
        title="添加用户授权"
        // width={500}
        onClose={onClose}
        open={open}
        closable={false}
        destroyOnClose
        forceRender
        extra={
          <Space>
            <Button onClick={onClose}>取消</Button>
            <Button type="primary" onClick={onSubmit} loading={oprLoading}>
              授权
            </Button>
          </Space>
        }
      >
        <ProForm<{
          name: string;
          company?: string;
          useMode?: string;
        }>
          submitter={false}
          onFinish={async (values) => {
            setOprLoading(true);
            const userIds = [];
            const users = [];
            selectValue?.forEach((item) => {
              userIds.push(item.value);
              users.push({ principal: item.value, action: values?.action });
            });
            const userDatas = userData;
            console.log('userDatas', userDatas);
            console.log('users', users);
            console.log(userDatas?.filter((user) => !userIds.includes(user.principal)));
            const userValues = (
              userDatas?.filter((user) => !userIds.includes(user.principal)) || []
            ).concat(users);
            console.log(current, values, userData, orgData, userIds, userValues);
            const response = await fakeSubmitForm({
              appId: current,
              orgs: orgData || [],
              users: userValues,
            });
            console.log(response);
            setOprLoading(false);
            if (response.success == true) {
              notification.success({ message: '新增成功' });
              // history.push('/service/list');
              setOpen(false);
              setUserLoading(true);
              queryFakeItem({
                id: current,
              }).then(function (resp) {
                setUserData(resp?.data?.users || []);
                setUserLoading(false);
              });
              return true;
            } else {
              notification.error({ message: '新增失败:' + response?.msg });
              return false;
            }
          }}
          formRef={formRef}
          autoFocusFirstInput
        >
          <ProFormItem label="选择用户" name="userIds">
            <UserSearch selectValue={selectValue} onChange={(v) => setSelectValue(v)} />
          </ProFormItem>
          <ProFormRadio.Group
            name="action"
            label="动作"
            initialValue={'Allowed'}
            options={[
              {
                label: '允许',
                value: 'Allowed',
              },
              {
                label: '拒绝',
                value: 'Denied',
              },
            ]}
          />
        </ProForm>
      </Drawer>
      <Drawer
        title="添加部门授权"
        // width={500}
        onClose={onCloseOrg}
        open={openOrg}
        closable={false}
        destroyOnClose
        forceRender
        extra={
          <Space>
            <Button onClick={onCloseOrg}>取消</Button>
            <Button type="primary" onClick={onSubmitOrg} loading={oprLoading}>
              授权
            </Button>
          </Space>
        }
      >
        <ProForm<{
          name: string;
          company?: string;
          useMode?: string;
        }>
          submitter={false}
          onFinish={async (values) => {
            setOprLoading(true);
            const orgIds = [];
            const orgs = [];
            selectValueOrg?.forEach((item) => {
              orgIds.push(item.value);
              orgs.push({ id: item.value, action: values?.action });
            });
            const orgDatas = orgData;
            const orgValues = (orgDatas?.filter((org) => !orgIds.includes(org.id)) || []).concat(
              orgs,
            );
            console.log(current, values, userData, orgData, orgIds, orgValues);
            const response = await fakeSubmitForm({
              appId: current,
              orgs: orgValues || [],
              users: userData || [],
            });
            setOprLoading(false);
            console.log(response);
            if (response.success == true) {
              notification.success({ message: '新增成功' });
              // history.push('/service/list');
              setOpenOrg(false);
              setOrgLoading(true);
              queryFakeItem({
                id: current,
              }).then(function (resp) {
                setOrgData(resp?.data?.orgs || []);
                setOrgLoading(false);
              });
              return true;
            } else {
              notification.error({ message: '新增失败:' + response?.msg });
              return false;
            }
          }}
          formRef={formRefOrg}
          autoFocusFirstInput
        >
          <ProFormItem label="选择部门" name="orgIds">
            <OrgSearch selectValue={selectValueOrg} onChange={(v) => setSelectValueOrg(v)} />
          </ProFormItem>
          <ProFormRadio.Group
            name="action"
            label="动作"
            initialValue={'Allowed'}
            options={[
              {
                label: '允许',
                value: 'Allowed',
              },
              {
                label: '拒绝',
                value: 'Denied',
              },
            ]}
          />
        </ProForm>
      </Drawer>

      <ProCard bordered title={<></>} subTitle={"如果没有授权任何用户和部门允许访问，则默认所有用户允许访问。"}>
        <Row gutter={0}>
          <Col span={5} style={{ borderRight: '1px solid rgba(0, 0, 0, 0.06)', backgroundColor: '#fff' }}>
            <Input.Search onSearch={onSearch} style={{ padding: '8px 8px 8px 8px' }} />
            <Menu
              className={styles.antMenuInner}
              onClick={onClick}
              selectedKeys={[current]}
              mode="vertical"
              items={menuData}
              style={{ maxHeight: '760px', overflow: 'auto', borderRight: 'none', marginRight: 8, marginLeft: 8 }}
            />
          </Col>
          <Col span={19}>
            {current ? (
              <>
                {userTable()}
                {orgTable()}
              </>
            ) : (
              <Result
                // title="授权"
                icon={
                  <img src="https://gw.alipayobjects.com/zos/antfincdn/ZHrcdLPrvN/empty.svg"></img>
                }
              // subTitle="选择应用，并控制谁可以访问"
              />
            )}
            {/* </Card> */}
          </Col>
        </Row>
      </ProCard>
    </PageContainer>
  );
};

export default CardList;
