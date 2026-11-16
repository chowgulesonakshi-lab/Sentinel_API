import axios from "axios";

const API = "http://localhost:8080/services";

export const getServices = async () => {
    const response = await axios.get(API);
    return response.data;
};

export const getServicesByProject = async (projectId) => {
    const response = await axios.get(`${API}/project/${projectId}`);
    return response.data;
};

export const createService = async (service) => {
    const response = await axios.post(API, service);
    return response.data;
};

export const updateService = async (id, service) => {
    const response = await axios.put(`${API}/id/${id}`, service);
    return response.data;
};

export const deleteService = async (id) => {
    await axios.delete(`${API}/id/${id}`);
};