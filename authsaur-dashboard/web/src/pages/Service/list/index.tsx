import { getProtocolLogo, getServiceType } from '@/tools';
import { PlusOutlined, ShoppingOutlined } from '@ant-design/icons';
import { ProList } from '@ant-design/pro-components';
import { PageContainer } from '@ant-design/pro-layout';
import { Avatar, Button, Card, List, Tag, Tooltip } from 'antd';
import { history, Link } from 'umi';
import { queryServiceList } from './service';
import styles from './style.less';


export default () => {
  return (
    <PageContainer>
      <div className={styles.cardList}>
        <ProList<any>
          pagination={{
            defaultPageSize: 24,
            showSizeChanger: false,
          }}
          
          search={{
            collapsed: false,
            defaultCollapsed: false,
            collapseRender: false,
            layout: 'vertical',
            
          }}
          split={false}
          grid={{ gutter: 16, xs: 1, sm: 1, md: 1, lg: 2, xl: 3, xxl: 4 }}
          toolbar={{
            actions: [
              <Button
                key="market" icon={<ShoppingOutlined />} type="primary" onClick={() => history.push('/service/method')}>
                去应用市场创建
              </Button>,
              <Button key="create" icon={<PlusOutlined />} type="primary" onClick={() => history.push(`/service/protocol`)}>
                创建应用
              </Button>,
            ],
          }}
          metas={{
            title: {
              dataIndex: 'name',
              title: '应用名称',
            },
            // type: {
            //   title: '类型',
            //   valueType: 'select',
            //   valueEnum: {
            //     '': { text: '全部' },
            //     CAS: {
            //       text: 'CAS协议',
            //     },
            //     SAML: {
            //       text: 'SAML协议',
            //     },
            //     OAuth2: {
            //       text: 'OAUTH2协议',
            //     },
            //     Oidc: {
            //       text: 'OIDC协议',
            //     },
            //   },
            // },
            tag: {
              dataIndex: 'tag',
              title: '标签分类',
            },
            serviceId: {
              dataIndex: 'serviceId',
              title: '地址',
            },
          }}
          request={queryServiceList}
          rowKey="id"
          renderItem={(item) => {
            const type = item?.type || getServiceType(item);
            return (
              <List.Item key={item.id}>
                <Tooltip title={item.description} color={'var(--ant-primary-color)'}>
                  <Link to={`/service/edit/${item.id}/${type}`}>
                    <Card
                      hoverable
                      bordered={false}
                      className={styles.card}
                      style={{ height: '100%',backgroundColor:'#f7f8fa' }}
                    >
                      <Card.Meta

                        avatar={
                          <Avatar
                            shape="square"
                            style={{width:'60px',height:'60px'}}
                            src={
                              item.logo || getProtocolLogo(type)
                            }
                          />
                        }
                        title={<a>{item.name}</a>}
                        description={
                          <>
                            <Tag>{type}</Tag>
                            {item?.tag && <Tag>{item?.tag}</Tag>}
                            <p/>
                            {/* <Paragraph ellipsis>
                              {item.serviceId}
                            </Paragraph> */}
                            {/* <p></p> */}
                            {/* 标签：{item?.tag || '无'} */}
                          </>
                        }
                      />
                    </Card>
                  </Link>
                </Tooltip>
              </List.Item>
            );
          }}
        />
      </div>
    </PageContainer>
  );
};