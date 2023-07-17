import { standardServices } from '@/tools';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { ProList } from '@ant-design/pro-components';
import { PageContainer } from '@ant-design/pro-layout';
import { Breadcrumb, Card, List, Space, Tooltip } from 'antd';
import { Link } from 'umi';

export type Member = {
  avatar: any;
  realName: string;
  nickName: string;
  email: string;
  outUserNo: string;
  phone: string;
  permission?: string[];
};

const items = Array.from(standardServices.values());

const CardList = () => {
  return (
    <PageContainer
      content={<Breadcrumb>
        <Link to="/service/list">
          <Space>
            <ArrowLeftOutlined />
            <span>返回</span>
          </Space>
        </Link>
      </Breadcrumb>}
    >
      {/* <Card title="应用市场" ghost> */}
      <ProList
        // bordered
        grid={{ gutter: 16, xs: 1, sm: 1, md: 1, lg: 2, xl: 2, xxl: 2 }}
        dataSource={items}
        // style={{paddingTop:24}}
        headerTitle={<div></div>}
        split={false}
        pagination={false}
        // itemLayout="horizontal"
        renderItem={item => (
          <List.Item key={item.key}>
            <Link to={`/service/${item.key}/add`}>
              <Card
                // title={item.label}
                bordered={false}
                hoverable
                style={{ backgroundColor: '#f7f8fa' }}
              >
                <List.Item.Meta
                  avatar={item.avatar}
                  title={item.title}
                  description={
                    <Tooltip title={item.description}>
                      <div style={{ lineClamp: 2, WebkitLineClamp: 2, boxOrient: 'vertical', WebkitBoxOrient: 'vertical', overflow: 'hidden', display: '-webkit-box', }}>
                        {item.description}
                      </div>
                    </Tooltip>
                  }
                />
              </Card>
            </Link>
          </List.Item>
        )
        }
      />
      {/* </Card> */}
    </PageContainer >
  );
};

export default CardList;
