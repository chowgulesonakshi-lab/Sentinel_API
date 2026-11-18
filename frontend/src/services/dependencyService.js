import api from "../api/axios";

export const getDependencyGraph = async (projectId) => {
    const response = await api.get(`/service-dependencies/project/${projectId}/graph`);
    return response.data;
};

export const getDependencies = async (projectId) => {
    const response = await api.get(`/service-dependencies/project/${projectId}`);
    return response.data;
};

export const createDependency = async (dependency) => {
    const response = await api.post("/service-dependencies", dependency);
    return response.data;
};

export const deleteDependency = async (id) => {
    await api.delete(`/service-dependencies/${id}`);
};

export const getDependencyImpact = async (projectId, serviceId) => {
    const response = await api.get(`/service-dependencies/project/${projectId}/impact/${serviceId}`);
    return response.data;
};
