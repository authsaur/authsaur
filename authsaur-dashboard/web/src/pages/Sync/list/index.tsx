import { PageContainer } from '@ant-design/pro-layout';
import ProList from '@ant-design/pro-list';
import { Avatar, Card, List } from 'antd';
import Meta from 'antd/lib/card/Meta';

const data = [
  {
    title: 'Title 1',
  },
  {
    title: 'Title 2',
  },
  {
    title: 'Title 3',
  },
  {
    title: 'Title 4',
  },
  {
    title: 'Title 4',
  },
  {
    title: 'Title 4',
  },
  {
    title: 'Title 4',
  },
];

const CardList = () => {
  return (
    <PageContainer>
      <ProList
        headerTitle="请选择同步方式"
        // toolbar={{
        //   search: {
        //     onSearch: (value: string) => {
        //       alert(value);
        //     },
        //   },
        //   actions: [
        //     // <Button type="primary" key="primary">
        //     //   创建实验
        //     // </Button>,
        //   ],
        // }}
        // pagination={{
        //   defaultPageSize: 20,
        //   showSizeChanger: true,
        // }}
        grid={{ gutter: 16, column: 4 }}
        dataSource={data}
        renderItem={item => (
          <List.Item>
            <Card
            // actions={[
            //   <SettingOutlined key="setting" />,
            //   <EditOutlined key="edit" />,
            //   <EllipsisOutlined key="ellipsis" />,
            // ]}
            >
              <Meta
                avatar={<Avatar src="/cas.svg" size="large" shape='square' style={{ backgroundColor: '#87d068' }} />}
                title="Card title"
                description={<div style={{ height: 48 }}>This is the description</div>}
              />
            </Card>
          </List.Item>
        )}
      />
    </PageContainer>
  );
};

export default CardList;
