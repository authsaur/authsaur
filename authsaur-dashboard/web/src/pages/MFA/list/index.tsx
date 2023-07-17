import { PageContainer } from '@ant-design/pro-layout';
import ProList from '@ant-design/pro-list';
import { Avatar, Card, List, Switch } from 'antd';

const data = [
  {
    title: '短信',
  },
  {
    title: '邮箱',
  },
  {
    title: 'OTP',
  },

];

const CardList = () => {
  return (
    <PageContainer>
      <Card
        title="MFA">
        <ProList
          headerTitle="基础列表"
          grid={{ gutter: 0, column: 1 }}
          dataSource={data}
          split={true}
          // itemLayout="horizontal"
          renderItem={item => (
            <List.Item
              actions={[<Switch defaultChecked />]}>

              <List.Item.Meta
                avatar={<Avatar src="/cas.svg" size="large" shape='square' style={{ backgroundColor: '#87d068' }} />}
                title={item.title}
                description={<>This is the description</>}
              />
            </List.Item>
          )
          }
        /></Card>
      <p></p>
      <Card
        title="MFA">
        <ProList
          // grid={{ gutter: 0, column: 1 }}
          dataSource={data}
          split={true}
          itemLayout="horizontal"
          renderItem={item => (
            <List.Item
              actions={[<Switch defaultChecked />]}>

              <List.Item.Meta
                avatar={<Avatar src="/cas.svg" size="large" shape='square' style={{ backgroundColor: '#87d068' }} />}
                title={item.title}
                description={<>This is the description</>}
              />
            </List.Item>
          )
          }
        /></Card>
    </PageContainer >
  );
};

export default CardList;
