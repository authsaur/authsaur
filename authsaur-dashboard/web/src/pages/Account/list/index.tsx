import { getAuthnHandler } from '@/tools';
import { addTreeData, delTreeData, editTreeData, updateTreeData } from '@/tree-utils';
import {
  ArrowLeftOutlined,
  ImportOutlined,
  PlusOutlined,
  SettingOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import {
  ActionType,
  ProColumns,
  ProForm,
  ProFormText,
  ProTable,
  TableDropdown
} from '@ant-design/pro-components';
import { ProFormItem, ProFormSwitch } from '@ant-design/pro-form';
import { PageContainer } from '@ant-design/pro-layout';
import {
  Alert,
  Breadcrumb,
  Button,
  Card,
  Col,
  Drawer,
  Dropdown,
  FormInstance,
  MenuProps,
  Modal,
  Row,
  Space,
  Tag,
  Tree,
  Typography,
  notification
} from 'antd';
import React, { useRef, useState } from 'react';
import { Link } from 'umi';
import 'yet-another-abortcontroller-polyfill';
import OrgSearch from '../OrgSearch';
import {
  fakeSubmitOrgForm,
  fakeSubmitUserForm,
  queryFakeUserItem,
  queryFakeUserList,
  querySubOrgList,
  removeFakeItem,
  removeFakeUserItem,
} from '../view/service';

const Page: React.FC = (props) => {
  const source = props.match.params;

  const [treeData, setTreeData] = useState();
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);
  const [selectedTitle, setSelectedTitle] = useState<React.Key>();
  const [addState, setAddState] = useState<boolean>();
  const [addOrgState, setAddOrgState] = useState<boolean>();
  const [autoExpandParent, setAutoExpandParent] = useState<boolean>(true);
  const [selectedUser, setSelectedUser] = useState<string>();
  const [userList, setUserList] = useState<[]>();
  const [userLoading, setUserLoading] = useState(false);
  const [controller, setController] = useState();
  const searchRef = useRef<FormInstance>();
  const actionRef = useRef<ActionType>();
  const formRef = useRef<FormInstance>();
  const formRefUser = useRef<FormInstance>();

  const [org, setOrg] = useState<{}>();
  const [loadedKeys, setLoadedKeys] = useState<string[]>([]);

  const [open, setOpen] = useState(false);
  const [openUser, setOpenUser] = useState(false);

  const [openDel, setOpenDel] = useState(false);
  const [openUserDel, setOpenUserDel] = useState(false);
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [addUserLoading, setAddUserLoading] = useState(false);
  const [addOrgLoading, setAddOrgLoading] = useState(false);
  const [pageLoading, setPageLoading] = useState(false);


  const [selectValueOrg, setSelectValueOrg] = useState();
  const [currentPage, setCurrentPage] = useState();
  const [totalPage, setTotalPage] = useState();

  const showDrawer = () => {
    setOpen(true);
    setAddOrgState(true);
    setAddOrgLoading(false);
    formRef.current?.setFieldsValue({
      parentId: selectedKeys[0],
      parentName: selectedTitle,
      name: '',
      id: undefined,
    });
  };
  const showDrawerEdit = () => {
    setOpen(true);
    setAddOrgState(false);
    setAddOrgLoading(false);
    formRef.current?.setFieldsValue({
      id: selectedKeys[0],
      name: selectedTitle,
      parentId: undefined,
    });
  };
  const showDrawerUser = () => {
    setAddState(true);
    setOpenUser(true);
    setAddUserLoading(false);
    formRefUser.current?.resetFields();
    const orgIds = [
      { label: selectedTitle, title: selectedTitle, value: selectedKeys[0], key: selectedKeys[0] },
    ];
    formRefUser.current?.setFieldsValue({
      orgIds,
    });
    setSelectValueOrg(orgIds);
  };
  const showDrawerUserEdit = (record) => {
    setAddState(false);
    setOpenUser(true);
    setAddUserLoading(false);
    formRefUser.current?.resetFields();
    setSelectValueOrg(undefined);
    queryFakeUserItem({ id: record.principal }).then(function (resp) {
      console.log(resp);
      const response = resp?.data || {};
      const orgIds = response.orgs.map((item) => {
        return { label: item.name, value: item.id, key: item.id };
      });
      formRefUser.current?.setFieldsValue({
        ...response,
        orgIds,
      });
      setSelectValueOrg(orgIds);
    });
  };

  const onClose = () => {
    setOpen(false);
  };
  const onSubmit = () => {
    formRef.current?.submit();
  };
  const onCloseUser = () => {
    setOpenUser(false);
  };
  const onSubmitUser = () => {
    formRefUser.current?.submit();
  };

  const handleMenuClick: MenuProps['onClick'] = (e) => {
    console.log('click', e);
    // history.push('/authentication/add');
    if (e.key == 1) {
      showDrawer();
    }
    if (e.key == 2) {
      showDrawerEdit();
    }
    if (e.key == 3) {
      showModalDel();
    }
  };
  const menuProps = {
    items: [
      {
        key: '1',
        label: '新增子部门',
      },
      {
        key: '2',
        label: '编辑',
      },
      {
        key: '3',
        label: '删除',
      },
    ],
    onClick: handleMenuClick,
  };

  const columns: ProColumns<any>[] = [
    {
      title: '用户名',
      dataIndex: 'principal',
      key: 'name',
      ellipsis: true,
      copyable: true,
      // render: (_, record) => (
      //   <Tooltip title={record.principal}>{record.userId}</Tooltip>
      // ),
    },
    {
      title: '姓名',
      dataIndex: 'name',
      ellipsis: true,
      hideInSearch: true,
    },
    {
      title: '状态',
      dataIndex: 'state',
      hideInSearch: true,
      render: (_, record) => (record.state ? <Tag>禁用</Tag> : <Tag>有效</Tag>),
    },
    {
      title: '邮箱',
      key: 'email',
      dataIndex: 'email',
      ellipsis: true,
    },
    {
      title: '手机',
      key: 'phone',
      dataIndex: 'mobile',
      ellipsis: true,
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <TableDropdown
          key="actionGroup"
          onSelect={(key) => {
            console.log(key, record);
            if (key == 'copy') {
              showDrawerUserEdit(record);
            } else if (key == 'delete') {
              setSelectedUser(record.principal);
              showModalUserDel();
            }
          }}
          menus={[
            { key: 'copy', name: '编辑' },
            { key: 'delete', name: '删除' },
          ]}
        ></TableDropdown>,
      ],
    },
  ];

  const loadUserList = (orgId: string, params, page, pageSize) => {
    setUserLoading(true);
    console.log('abort', controller);
    if (controller) {
      controller.abort();
    }

    const controller11 = new AbortController(); // 创建一个控制器
    const { signal } = controller11;
    setController(controller11);
    if (!params) {
      params = searchRef?.current?.getFieldsValue();
    }
    queryFakeUserList(
      {
        source: source.source,
        orgId: orgId,
        current: page || 1,
        pageSize: pageSize || 10,
        ...params,
      },
      signal,
    )
      .then(function (response) {
        setUserLoading(false);
        console.log(orgId, selectedKeys);
        // if (selectedKeys[0] == orgId) {
        console.log(response);
        const data = response?.data?.list || [];
        setUserList(data);
        setController(undefined);
        setTotalPage(response?.data?.total || 0);
        // }
      })
      .catch(function (error) {
        console.log(error);
      });
  };

  const onSelect = (selectedKeysValue: React.Key[], info: any) => {
    //console.log("selected", selectedKeysValue, info);
    setSelectedKeys([info.node.key]);
    loadUserList(info.node.key);
    setSelectedTitle(info.node.title);
    setExpandedKeys((origin) => {
      // console.log(origin);
      return origin.concat(selectedKeysValue);
    });
  };

  const onExpand = (expandedKeysValue: React.Key[], info) => {
    // console.log('onExpand', expandedKeysValue, info);
    // if not set autoExpandParent to false, if children expanded, parent can not collapse.
    // or, you can remove all expanded children keys.
    setExpandedKeys(expandedKeysValue);
    setSelectedKeys([info.node.key]);
    loadUserList(info.node.key);
    setSelectedTitle(info.node.title);
    setAutoExpandParent(false);
    // actionRef?.current?.reload();
  };

  const onLoadData = ({ key, children }: any) => {
    setLoadedKeys((origin) => origin.concat(key));
    return new Promise<void>((resolve) => {
      console.log(children);
      if (children) {
        resolve();
        return;
      }
      querySubOrgList({ source: source.source, parentId: key }).then((response) => {
        console.log(response);
        const data = response?.data?.list || [];
        const tree = data.map((d) => {
          if (d.subOrgs > 0) {
            return {
              title: d.name,
              key: d.id,
              // isLeaf: true,
            };
          } else {
            return {
              title: d.name,
              key: d.id,
              children: [],
              // isLeaf: true,
            };
          }
        });
        console.log(tree);
        setTreeData((origin) => updateTreeData(origin, key, tree));

        resolve();
      });
    });
  };

  React.useEffect(() => {
    setPageLoading(true);
    querySubOrgList({
      source: source.source,
      parentId: '',
    }).then(function (response) {
      setPageLoading(false);
      console.log(response);
      const data = response?.data?.list || [];
      const tree = data.map((d) => {
        if (d.subOrgs > 0) {
          return {
            title: d.name,
            key: d.id,
            // isLeaf: true,
          };
        } else {
          return {
            title: d.name,
            key: d.id,
            children: [],
            // isLeaf: true,
          };
        }
      });
      console.log(tree);
      setTreeData(tree);
      setLoadedKeys([]);
      if (tree[0]) {
        setSelectedKeys([tree[0].key]);
        loadUserList(tree[0].key);
        setSelectedTitle(tree[0].title);
        setExpandedKeys([tree[0].key]);
      }
      setOrg(data[0]);
    });
  }, [source]);

 
  const showModalDel = () => {
    setOpenDel(true);
  };
  const showModalUserDel = () => {
    setOpenUserDel(true);
  };

  const handleOk = () => {
    setConfirmLoading(true);
    removeFakeItem({ id: selectedKeys[0] }).then((response) => {
      console.log(response);
      setConfirmLoading(false);
      if (response.success == true) {
        notification.success({ message: '删除成功' });
        setOpenDel(false);
        setTreeData((origin) => delTreeData(origin, selectedKeys[0]));
        return true;
      } else {
        notification.error({ message: '删除失败:' + response?.msg });
        return false;
      }
    });
  };

  const handleCancel = () => {
    setOpenDel(false);
  };

  const handleUserOk = () => {
    setConfirmLoading(true);
    removeFakeUserItem({ id: selectedUser }).then((response) => {
      console.log(response);
      setConfirmLoading(false);
      if (response.success == true) {
        notification.success({ message: '删除成功' });
        setOpenUserDel(false);
        // actionRef?.current?.reload();
        loadUserList(selectedKeys[0], searchRef?.current?.getFieldsValue(), currentPage, 10);
        return true;
      } else {
        notification.error({ message: '删除失败:' + response?.msg });
        return false;
      }
    });
  };

  const handleUserCancel = () => {
    console.log('Clicked cancel button');
    setOpenUserDel(false);
  };


  return (
    <PageContainer
      loading={pageLoading}
      content={
        <Breadcrumb>
          <Link to="/member/view">
            <Space>
              <ArrowLeftOutlined />
              <span>返回</span>
            </Space>
          </Link>
        </Breadcrumb>
      }
    >
      <Row gutter={8} style={{ padding: '16px 8px', backgroundColor: '#fff' }}>
        <Col span={6}>
          <Card
            // bordered={false}
            style={{ height: '100%' }}
            bodyStyle={{ padding: '0px 0' }}
            title={
              <Card bordered={false} bodyStyle={{ padding: ' 0' }}>
                <Card.Meta avatar={getAuthnHandler(org?.type)?.smallIcon} title={org?.name} />
              </Card>
            }
            extra={
              <Space>
                <Dropdown menu={menuProps}>
                  <SettingOutlined />
                </Dropdown>
              </Space>
            }
          >
            {/* extra={
                <Space>
                  <Button type="primary" onClick={showDrawer}>新增</Button>
                  <Button type="primary" onClick={showDrawerEdit}>编辑</Button>
                  <Button type="primary" onClick={showModalDel} danger>删除</Button>
                </Space>
              }> */}
            <Tree
              blockNode
              onExpand={onExpand}
              expandedKeys={expandedKeys}
              autoExpandParent={autoExpandParent}
              showLine={true}
              // showIcon={false}
              selectable
              onSelect={onSelect}
              selectedKeys={selectedKeys}
              loadData={onLoadData}
              loadedKeys={loadedKeys}
              treeData={treeData}
            />
          </Card>
        </Col>
        <Col span={18}>
          <ProTable<any>
            headerBordered
            columns={columns}
            actionRef={actionRef}
            formRef={searchRef}
            cardBordered
            headerTitle={
              <Space>
                <TeamOutlined />
                {selectedTitle}
              </Space>
            }
            options={false}
            search={{
              labelWidth: 'auto',
              collapsed: false,
              defaultCollapsed: false,
              collapseRender: false,
              // optionRender: false,
            }}
            // debounceTime={}
            params={{ orgId: selectedKeys[0] }}
            loading={userLoading}
            dataSource={userList}
            onSubmit={(params) => {
              console.log(params, selectedKeys[0]);
              loadUserList(selectedKeys[0], params, 1, 10);
            }}
            rowKey="principal"
            pagination={{
              pageSize: 10,
              showSizeChanger: false,
              total: totalPage,
              current: currentPage,
              // hideOnSinglePage: true,
              onChange: (page: number, pageSize: number) => {
                console.log(page, pageSize, searchRef?.current?.getFieldsValue());
                setCurrentPage(page);
                loadUserList(selectedKeys[0], searchRef?.current?.getFieldsValue(), page, pageSize);
              },
            }}
            onChange={(pagination) => {
              console.log(pagination);
            }}
            toolBarRender={() => [
              <Button
                key="button"
                icon={<ImportOutlined />}
                disabled
                type="primary"
                onClick={showDrawerUser}
              >
                导入用户
              </Button>,
              <Button key="button" icon={<PlusOutlined />} type="primary" onClick={showDrawerUser}>
                创建用户
              </Button>,
            ]}
          />
        </Col>
      </Row>

      <Drawer
        title={addState ? '新增用户' : '编辑用户'}
        width={460}
        onClose={onCloseUser}
        open={openUser}
        closable={false}
        destroyOnClose
        forceRender
        extra={
          <Space>
            <Button onClick={onCloseUser}>取消</Button>
            <Button type="primary" onClick={onSubmitUser} loading={addUserLoading}>
              {addState ? '创建' : '更新'}
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
          // layout="horizontal"
          // labelCol={{ span: 6 }}
          // wrapperCol={{ span: 14 }}
          onFinish={async (values) => {
            setAddUserLoading(true);
            console.log(values, selectValueOrg);
            const orgIds = [];
            selectValueOrg?.forEach((item) => {
              orgIds.push(item.value);
            });
            const params = { ...values, orgIds: orgIds };
            const response = await fakeSubmitUserForm(params);
            console.log(response);
            setAddUserLoading(false);
            if (response.success == true) {
              notification.success({ message: '新增成功' });
              setOpenUser(false);
              // actionRef.current?.reload();
              loadUserList(selectedKeys[0], searchRef?.current?.getFieldsValue(), currentPage, 10);
              return true;
            } else {
              notification.error({ message: '新增失败:' + response?.msg });
              return false;
            }
          }}
          formRef={formRefUser}
          autoFocusFirstInput
        >
          <ProFormItem
            label="归属部门"
            name="orgIds"
            rules={[{ required: true, message: '请选择部门' }]}
          >
            <OrgSearch
              source={source.source}
              selectValue={selectValueOrg}
              onChange={(v) => setSelectValueOrg(v)}
            />
          </ProFormItem>
          <ProFormText name="principal" hidden />
          {addState ? (
            <ProFormText
              label="用户名"
              width="xl"
              name="userId"
              rules={[{ required: true, message: '请输入用户名' }]}
            />
          ) : (
            <ProFormText label="用户名" width="xl" name="userId" readonly />
          )}
          <ProFormText
            label={addState ? '密码' : '重置密码'}
            width="xl"
            name="password"
            fieldProps={{
              autoComplete: 'new-password',
            }}
          />
          {/* <ProFormItem>
              <Button type="primary">自动生成密码</Button>
            </ProFormItem> */}
          <ProFormText label="姓名" width="xl" name="name" />
          <ProFormText label="邮箱" width="xl" name="email" />
          <ProFormText label="手机" width="xl" name="mobile" />
          <ProFormSwitch label="禁用用户" name="state" initialValue={0}></ProFormSwitch>
        </ProForm>
      </Drawer>
      <Drawer
        title={addOrgState ? '新增部门' : '编辑部门'}
        // width={500}
        onClose={onClose}
        open={open}
        closable={false}
        destroyOnClose
        forceRender
        extra={
          <Space>
            <Button onClick={onClose}>取消</Button>
            <Button type="primary" onClick={onSubmit} loading={addOrgLoading}>
              {addOrgState ? '创建' : '更新'}
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
            // console.log(values);
            setAddOrgLoading(true);
            const response = await fakeSubmitOrgForm(values);
            // console.log(response);
            setAddOrgLoading(false);
            if (response.success == true) {
              notification.success({ message: '新增成功' });
              // history.push('/service/list');
              const d = response?.data;
              const tree = {
                title: d.name,
                key: d.id,
                children: [],
              };
              if (addOrgState) {
                setTreeData((origin) => addTreeData(origin, values?.parentId, tree));
              } else {
                setTreeData((origin) => editTreeData(origin, values?.id, tree));
              }
              setOpen(false);
              return true;
            } else {
              notification.error({ message: '新增失败:' + response?.msg });
              return false;
            }
          }}
          formRef={formRef}
          autoFocusFirstInput
        >
          <ProFormText label="上级部门" width="xl" name="parentId" hidden />
          <ProFormText width="xl" name="id" hidden />

          {addOrgState && <ProFormText label="上级部门" width="xl" name="parentName" readonly />}

          <ProFormText
            label="部门名称"
            width="xl"
            name="name"
            rules={[{ required: true, message: '请输入名称' }]}
            placeholder="请输入名称"
          />
        </ProForm>
      </Drawer>

      <Modal
        open={openDel}
        okType="danger"
        onOk={handleOk}
        okButtonProps={{ type: 'primary' }}
        confirmLoading={confirmLoading}
        onCancel={handleCancel}
      >
        <Alert
          showIcon
          style={{ marginTop: '32px', padding: '24px' }}
          message={
            <>
              正在删除部门：<Typography.Text strong>{selectedTitle}</Typography.Text>
              ，此操作不可恢复，请谨慎操作！
            </>
          }
          type="warning"
        />
      </Modal>
      <Modal
        closable={false}
        open={openUserDel}
        okType="danger"
        onOk={handleUserOk}
        okButtonProps={{ type: 'primary' }}
        confirmLoading={confirmLoading}
        onCancel={handleUserCancel}
      >
        <Alert
          showIcon
          style={{ marginTop: '32px', padding: '24px' }}
          message={
            <>
              正在删除用户：<Typography.Text strong>{selectedUser}</Typography.Text>
              ，此操作不可恢复，请谨慎操作！
            </>
          }
          type="warning"
        />
      </Modal>
    </PageContainer>
  );
};

export default Page;
