import { useEffect, useMemo, useState } from "react";
import MainLayout from "../layouts/MainLayout";
import { getProjects } from "../services/projectService";
import { getServicesByProject } from "../services/apiServiceService";
import {
    createDependency,
    deleteDependency,
    getDependencies,
    getDependencyGraph,
    getDependencyImpact
} from "../services/dependencyService";
import { GitBranch, Trash2 } from "lucide-react";

function Dependencies() {
    const [projects, setProjects] = useState([]);
    const [projectId, setProjectId] = useState("");
    const [graph, setGraph] = useState({ nodes: [], edges: [] });
    const [dependencies, setDependencies] = useState([]);
    const [services, setServices] = useState([]);
    const [selectedService, setSelectedService] = useState(null);
    const [impact, setImpact] = useState(null);
    const [form, setForm] = useState({ dependentServiceId: "", dependencyServiceId: "" });

    const loadProject = async (id) => {
        if (!id) return;
        const [nextGraph, nextDependencies, nextServices] = await Promise.all([
            getDependencyGraph(id),
            getDependencies(id),
            getServicesByProject(id)
        ]);
        setGraph(nextGraph);
        setDependencies(nextDependencies);
        setServices(nextServices);
        setForm({ dependentServiceId: "", dependencyServiceId: "" });
        setSelectedService(null);
        setImpact(null);
    };

    useEffect(() => {
        getProjects().then((data) => {
            setProjects(data);
            if (data.length > 0) setProjectId(String(data[0].id));
        }).catch((error) => console.error("Failed to load projects", error));
    }, []);

    useEffect(() => {
        if (!projectId) return;
        let cancelled = false;
        const load = async () => {
            const [nextGraph, nextDependencies, nextServices] = await Promise.all([
                getDependencyGraph(projectId),
                getDependencies(projectId),
                getServicesByProject(projectId)
            ]);
            if (cancelled) return;
            setGraph(nextGraph);
            setDependencies(nextDependencies);
            setServices(nextServices);
            setForm({ dependentServiceId: "", dependencyServiceId: "" });
            setSelectedService(null);
            setImpact(null);
        };
        load().catch((error) => console.error("Failed to load dependency graph", error));
        return () => { cancelled = true; };
    }, [projectId]);

    const positions = useMemo(() => {
        const columns = Math.max(1, Math.ceil(Math.sqrt(graph.nodes.length)));
        return new Map(graph.nodes.map((node, index) => [node.id, {
            x: 120 + (index % columns) * 230,
            y: 80 + Math.floor(index / columns) * 130
        }]));
    }, [graph.nodes]);

    const refresh = () => loadProject(projectId).catch((error) => console.error("Failed to refresh dependencies", error));

    const handleCreate = async (event) => {
        event.preventDefault();
        try {
            await createDependency({
                dependentServiceId: Number(form.dependentServiceId),
                dependencyServiceId: Number(form.dependencyServiceId)
            });
            await refresh();
        } catch (error) {
            alert(error.response?.data?.message || "Unable to create dependency");
        }
    };

    const inspectService = async (serviceId) => {
        setSelectedService(serviceId);
        try {
            setImpact(await getDependencyImpact(projectId, serviceId));
        } catch (error) {
            console.error("Failed to load dependency impact", error);
        }
    };

    return (
        <MainLayout>
            <div className="space-y-6">
                <div className="flex flex-wrap items-end justify-between gap-4">
                    <div>
                        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Service Dependencies</h1>
                        <p className="mt-2 text-slate-500 dark:text-slate-400">Map service relationships and inspect potential cascading impact.</p>
                    </div>
                    <select value={projectId} onChange={(event) => setProjectId(event.target.value)} className="px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white">
                        {projects.map((project) => <option key={project.id} value={project.id}>{project.name}</option>)}
                    </select>
                </div>

                <form onSubmit={handleCreate} className="flex flex-wrap items-end gap-3 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5">
                    <label className="flex-1 min-w-52 text-xs font-semibold text-slate-500">SERVICE DEPENDS ON
                        <select required value={form.dependentServiceId} onChange={(event) => setForm({ ...form, dependentServiceId: event.target.value })} className="mt-1 w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white">
                            <option value="">Select service</option>{services.map((service) => <option key={service.id} value={service.id}>{service.name}</option>)}
                        </select>
                    </label>
                    <label className="flex-1 min-w-52 text-xs font-semibold text-slate-500">DEPENDS ON
                        <select required value={form.dependencyServiceId} onChange={(event) => setForm({ ...form, dependencyServiceId: event.target.value })} className="mt-1 w-full px-3 py-2 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white">
                            <option value="">Select dependency</option>{services.map((service) => <option key={service.id} value={service.id}>{service.name}</option>)}
                        </select>
                    </label>
                    <button className="px-4 py-2 rounded-lg bg-blue-600 text-white font-semibold">Add relationship</button>
                </form>

                <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 overflow-auto">
                    <div className="flex items-center gap-2 mb-4 text-slate-900 dark:text-white font-semibold"><GitBranch size={18} /> Dependency graph <span className="text-xs font-normal text-slate-400">Arrow direction: dependent → dependency</span></div>
                    <div className="relative min-w-[700px] min-h-[360px] bg-slate-50 dark:bg-slate-950 rounded-xl overflow-hidden">
                        <svg className="absolute inset-0 w-full h-full pointer-events-none">
                            <defs><marker id="dependency-arrow" markerWidth="8" markerHeight="8" refX="7" refY="3" orient="auto"><path d="M0,0 L0,6 L7,3 z" fill="#64748b" /></marker></defs>
                            {graph.edges.map((edge) => {
                                const from = positions.get(edge.dependentServiceId);
                                const to = positions.get(edge.dependencyServiceId);
                                return from && to ? <line key={edge.id} x1={from.x} y1={from.y} x2={to.x} y2={to.y} stroke="#94a3b8" strokeWidth="2" markerEnd="url(#dependency-arrow)" /> : null;
                            })}
                        </svg>
                        {graph.nodes.map((node) => {
                            const position = positions.get(node.id);
                            const healthClass = node.health === "UNHEALTHY" ? "border-red-400 bg-red-50 dark:bg-red-950/40" : node.health === "HEALTHY" ? "border-emerald-400 bg-emerald-50 dark:bg-emerald-950/40" : "border-slate-300 bg-white dark:bg-slate-900";
                            return <button key={node.id} onClick={() => inspectService(node.id)} style={{ left: position.x - 85, top: position.y - 32 }} className={`absolute w-44 p-3 rounded-xl border-2 text-left shadow-sm ${healthClass}`}><div className="font-semibold text-slate-900 dark:text-white truncate">{node.name}</div><div className="text-[11px] text-slate-500">{node.health} · {node.status}</div></button>;
                        })}
                        {graph.nodes.length === 0 && <p className="p-8 text-slate-500">Add services to this project to build a dependency graph.</p>}
                    </div>
                </div>

                <div className="grid lg:grid-cols-2 gap-6">
                    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5">
                        <h2 className="font-semibold text-slate-900 dark:text-white mb-3">Relationships</h2>
                        <div className="space-y-2">{dependencies.map((dependency) => <div key={dependency.id} className="flex items-center justify-between gap-3 text-sm"><span className="text-slate-700 dark:text-slate-300">{dependency.dependentServiceName} → {dependency.dependencyServiceName}</span><button onClick={async () => { await deleteDependency(dependency.id); await refresh(); }} className="text-red-500"><Trash2 size={15} /></button></div>)}{dependencies.length === 0 && <p className="text-sm text-slate-500">No dependencies defined.</p>}</div>
                    </div>
                    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5">
                        <h2 className="font-semibold text-slate-900 dark:text-white mb-3">Impact analysis</h2>
                        {!selectedService || !impact ? <p className="text-sm text-slate-500">Select a service node to inspect direct, indirect, confirmed, and potential impact.</p> : <div className="space-y-2 text-sm text-slate-600 dark:text-slate-300"><p>Direct dependents: {impact.directDependentServiceIds.length}</p><p>Indirect dependents: {impact.indirectDependentServiceIds.length}</p><p>Confirmed unhealthy: {impact.confirmedUnhealthyServiceIds.length}</p><p>Potentially affected: {impact.potentiallyAffectedServiceIds.length}</p></div>}
                    </div>
                </div>
            </div>
        </MainLayout>
    );
}

export default Dependencies;
