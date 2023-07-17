import { request } from 'umi';

export async function fakeSubmitForm(params: any) {
  return request('/api/service', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateZentao(params: any) {
  return request('/api/service/saveOrUpdateZentao', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateGitlab(params: any) {
  return request('/api/service/saveOrUpdateGitlab', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateJenkins(params: any) {
  return request('/api/service/saveOrUpdateJenkins', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateSentry(params: any) {
  return request('/api/service/saveOrUpdateSentry', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateSonarqube(params: any) {
  return request('/api/service/saveOrUpdateSonarqube', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateGrafana(params: any) {
  return request('/api/service/saveOrUpdateGrafana', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateJumpServer(params: any) {
  return request('/api/service/saveOrUpdateJumpServer', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateGerrit(params: any) {
  return request('/api/service/saveOrUpdateGerrit', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateJira(params: any) {
  return request('/api/service/saveOrUpdateJira', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateConfluence(params: any) {
  return request('/api/service/saveOrUpdateConfluence', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateSeafile(params: any) {
  return request('/api/service/saveOrUpdateSeafile', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateAuthsaurAdmin(params: any) {
  return request('/api/service/saveOrUpdateAuthsaurAdmin', {
    method: 'POST',
    data: params,
  });
}
export async function saveOrUpdateCodesign(params: any) {
  return request('/api/service/saveOrUpdateCodesign', {
    method: 'POST',
    data: params,
  });
}
export async function fakeSubmitUpdateForm(params: any) {
  return request('/api/service', {
    method: 'POST',
    data: params,
  });
}
export async function fakeSubmitAttributeRelease(params: any) {
  return request('/api/service/attribute', {
    method: 'POST',
    data: params,
  });
}

export async function queryFakeItem(params: any) {
  return request(`/api/service/${params.id}`, {
    method: 'GET',
  });
}
export async function removeFakeItem(params: any) {
  return request(`/api/service/${params.id}`, {
    method: 'DELETE',
  });
}
