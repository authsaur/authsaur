import { request } from 'umi';


export async function queryFakeOrgList(params: {
  count: number;
}): Promise<{ data: { list: any[] } }> {
  return request('/api/organization/search', {
    params,
  });
}

