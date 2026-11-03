import axios from "axios";

const API = "http://localhost:8080/service-dependencies";

export const getDependencyGraph = async (projectId) => {
    const response = await axios.get(`${API}/project/${projectId}/graph`);
    return response.data;
};

export const getDependencies = async (projectId) => {
    const response = await axios.get(`${API}/project/${projectId}`);
    return response.data;
};

export const createDependency = async (dependency) => {
    const response = await axios.post(API, dependency);
    return response.data;
};

export const deleteDependency = async (id) => {
    await axios.delete(`${API}/${id}`);
};

export const getDependencyImpact = async (projectId, serviceId) => {
    const response = await axios.get(`${API}/project/${projectId}/impact/${serviceId}`);
    return response.data;
};
