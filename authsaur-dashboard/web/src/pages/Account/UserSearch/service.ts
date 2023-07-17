import { request } from 'umi';



export async function queryFakeUserList(params: {
  count: number;
}): Promise<{ data: { list: any[] } }> {
  return request('/api/usermanagement/search', {
    params,
  });
}
