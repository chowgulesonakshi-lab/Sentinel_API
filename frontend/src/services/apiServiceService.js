import api from "../api/axios";

export const getServices = async () => {
    const response = await api.get("/services");
    return response.data;
};

export const getServicesByProject = async (projectId) => {
    const response = await api.get(`/services/project/${projectId}`);
    return response.data;
};

export const createService = async (service) => {
    const response = await api.post("/services", service);
    return response.data;
};

export const updateService = async (id, service) => {
    const response = await api.put(`/services/id/${id}`, service);
    return response.data;
};

export const deleteService = async (id) => {
    await api.delete(`/services/id/${id}`);
};
