const state = {
    user: null,
    token: null,
    message: null,
    error: null,
    view: 'login',
    classroomData: null,
    observers: [],
};

function subscribe(observer) {
    state.observers.push(observer);
}

function notify() {
    state.observers.forEach((observer) => observer(state));
}

function setState(patch) {
    Object.assign(state, patch);
    localStorage.setItem('appState', JSON.stringify({
        user: state.user,
        token: state.token,
        view: state.view
    }));
    notify();
}

function restoreState() {
    const saved = localStorage.getItem('appState');
    if (saved) {
        const parsed = JSON.parse(saved);
        if (parsed.token && parsed.user) {
            Object.assign(state, parsed);
            return true;
        }
    }
    return false;
}

function validate(email, contrasena, nombre = null, apellido = null, tipo = null) {
    const errors = [];
    
    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        errors.push('Correo válido requerido');
    }
    if (!contrasena || contrasena.length < 6) {
        errors.push('Contraseña mínimo 6 caracteres');
    }
    if (nombre !== null && (!nombre || nombre.trim().length < 2)) {
        errors.push('Nombre válido requerido');
    }
    if (apellido !== null && (!apellido || apellido.trim().length < 2)) {
        errors.push('Apellido válido requerido');
    }
    
    return errors;
}

function apiPost(path, body) {
    return fetch(path, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...(state.token && { 'Authorization': `Bearer ${state.token}` })
        },
        body: JSON.stringify(body),
    }).then(async (response) => {
        const data = await response.json().catch(() => null);
        if (!response.ok) {
            throw new Error((data && data.message) || response.statusText || 'Error de red');
        }
        return data;
    });
}

function loginContainer() {
    function submitLogin(event) {
        event.preventDefault();
        const form = event.target;
        const email = form.email.value.trim();
        const contrasena = form.contrasena.value.trim();
        
        const errors = validate(email, contrasena);
        if (errors.length > 0) {
            setState({ error: errors.join(', '), message: null });
            return;
        }

        setState({ message: null, error: null });
        apiPost('/auth/login', { email, contrasena })
            .then((data) => {
                setState({
                    user: data,
                    token: data.token,
                    view: 'dashboard',
                    message: 'Acceso exitoso.',
                    classroomData: generateMockClassroomData(data.tipo)
                });
            })
            .catch((err) => {
                setState({ error: 'Credenciales inválidas.', message: null });
            });
    }

    return renderForm({
        title: 'Iniciar sesión',
        description: 'Accede con tu correo y contraseña.',
        fields: [
            { label: 'Correo electrónico', name: 'email', type: 'email', placeholder: 'usuario@dominio.com' },
            { label: 'Contraseña', name: 'contrasena', type: 'password', placeholder: '••••••••' },
        ],
        submitLabel: 'Entrar',
        onSubmit: submitLogin,
        alternate: {
            text: '¿No tienes cuenta?',
            button: 'Registrarse',
            action: () => setState({ view: 'register', message: null, error: null }),
        },
    });
}

function registerContainer() {
    function submitRegister(event) {
        event.preventDefault();
        const form = event.target;
        const nombre = form.nombre.value.trim();
        const apellido = form.apellido.value.trim();
        const email = form.email.value.trim();
        const contrasena = form.contrasena.value.trim();
        const tipo = form.tipo.value;

        const errors = validate(email, contrasena, nombre, apellido);
        if (errors.length > 0) {
            setState({ error: errors.join(', '), message: null });
            return;
        }

        setState({ message: null, error: null });
        apiPost('/auth/registrar', { nombre, apellido, email, contrasena, tipo })
            .then((data) => {
                setState({
                    message: 'Registro completado. Inicia sesión para continuar.',
                    view: 'login',
                    error: null
                });
            })
            .catch((err) => {
                setState({ error: 'Error en el registro. Intenta con otro correo.', message: null });
            });
    }

    return renderForm({
        title: 'Registrarse',
        description: 'Crea una cuenta para acceder al libro de clases digital.',
        fields: [
            { label: 'Nombre', name: 'nombre', type: 'text', placeholder: 'Juan' },
            { label: 'Apellido', name: 'apellido', type: 'text', placeholder: 'Pérez' },
            { label: 'Correo electrónico', name: 'email', type: 'email', placeholder: 'usuario@dominio.com' },
            { label: 'Contraseña', name: 'contrasena', type: 'password', placeholder: '••••••••' },
            { label: 'Tipo de usuario', name: 'tipo', type: 'select', options: [
                { value: 'estudiante', label: 'Estudiante' },
                { value: 'profesor', label: 'Profesor' },
                { value: 'apoderado', label: 'Apoderado' },
            ] },
        ],
        submitLabel: 'Crear cuenta',
        onSubmit: submitRegister,
        alternate: {
            text: '¿Ya tienes cuenta?',
            button: 'Iniciar sesión',
            action: () => setState({ view: 'login', message: null, error: null }),
        },
    });
}

function dashboardContainer(user, classroomData) {
    function logout() {
        localStorage.removeItem('appState');
        setState({ user: null, token: null, view: 'login', message: 'Sesión cerrada.', error: null, classroomData: null });
    }

    function switchView(view) {
        setState({ view });
    }

    return renderDashboard(user, classroomData, logout, switchView);
}

function generateMockClassroomData(role) {
    if (role === 'profesor') {
        return {
            cursos: [
                { id: 1, nombre: 'Matemáticas 3ro A', estudiantes: 25, promedio: 6.8 },
                { id: 2, nombre: 'Matemáticas 3ro B', estudiantes: 23, promedio: 6.5 },
            ],
            evaluaciones: [
                { id: 1, nombre: 'Prueba 1', fecha: '2026-05-10', estudiantes: 25 },
                { id: 2, nombre: 'Prueba 2', fecha: '2026-05-15', estudiantes: 23 },
            ],
            alertas: [
                { id: 1, texto: 'Diego González no entregó tarea', fecha: '2026-05-12' },
            ],
        };
    } else if (role === 'estudiante') {
        return {
            cursos: [
                { id: 1, nombre: 'Matemáticas', profesor: 'Lic. García', calificacion: 7.2 },
                { id: 2, nombre: 'Lenguaje', profesor: 'Lic. López', calificacion: 8.1 },
                { id: 3, nombre: 'Ciencias', profesor: 'Dr. Martínez', calificacion: 6.9 },
            ],
            tareas: [
                { id: 1, asignatura: 'Matemáticas', titulo: 'Ejercicios Cap. 5', fecha: '2026-05-15', estado: 'pendiente' },
                { id: 2, asignatura: 'Lenguaje', titulo: 'Análisis de poema', fecha: '2026-05-13', estado: 'entregado' },
            ],
            asistencia: { presente: 42, ausente: 3, justificada: 2 },
        };
    } else if (role === 'apoderado') {
        return {
            hijos: [
                { id: 1, nombre: 'Diego González Pérez', curso: '3ro A', promedio: 7.1 },
                { id: 2, nombre: 'Carla González Pérez', curso: '1ro B', promedio: 8.3 },
            ],
            alertas: [
                { id: 1, tipo: 'calificacion', texto: 'Diego obtuvo 5.5 en Matemáticas', fecha: '2026-05-11' },
                { id: 2, tipo: 'asistencia', texto: 'Carla tuvo 2 ausencias en la semana', fecha: '2026-05-12' },
            ],
            comunicados: [
                { id: 1, de: 'Lic. García', asunto: 'Reunión de apoderados', fecha: '2026-05-20' },
            ],
        };
    }
    return {};
}

function renderForm({ title, description, fields, submitLabel, onSubmit, alternate }) {
    const form = document.createElement('form');
    form.className = 'card';
    form.addEventListener('submit', onSubmit);

    const titleEl = document.createElement('h1');
    titleEl.textContent = title;
    form.append(titleEl);

    const descEl = document.createElement('p');
    descEl.textContent = description;
    form.append(descEl);

    const fieldContainer = document.createElement('div');
    fieldContainer.className = 'form-grid';

    fields.forEach((field) => {
        const wrapper = document.createElement('div');
        const label = document.createElement('label');
        label.textContent = field.label;
        label.htmlFor = field.name;
        wrapper.append(label);

        let input;
        if (field.type === 'select') {
            input = document.createElement('select');
            input.name = field.name;
            input.id = field.name;
            field.options.forEach((optionData) => {
                const option = document.createElement('option');
                option.value = optionData.value;
                option.textContent = optionData.label;
                input.append(option);
            });
        } else {
            input = document.createElement('input');
            input.type = field.type;
            input.name = field.name;
            input.id = field.name;
            input.placeholder = field.placeholder || '';
            if (field.type === 'email') input.autocomplete = 'email';
        }
        wrapper.append(input);
        fieldContainer.append(wrapper);
    });

    form.append(fieldContainer);

    const submitButton = document.createElement('button');
    submitButton.type = 'submit';
    submitButton.className = 'button button-primary';
    submitButton.textContent = submitLabel;
    form.append(submitButton);

    const alternateRow = document.createElement('div');
    alternateRow.style.marginTop = '18px';
    alternateRow.innerHTML = `<span>${alternate.text}</span>`;
    const switchButton = document.createElement('button');
    switchButton.type = 'button';
    switchButton.className = 'button button-secondary';
    switchButton.textContent = alternate.button;
    switchButton.addEventListener('click', alternate.action);
    alternateRow.append(switchButton);
    form.append(alternateRow);

    return form;
}

function renderDashboard(user, classroomData, onLogout, switchView) {
    const container = document.createElement('div');

    const header = document.createElement('div');
    header.className = 'card header-row';
    const title = document.createElement('div');
    title.innerHTML = `<h1>Bienvenido, ${user.email}</h1><p>Has iniciado sesión como <strong>${capitalizeFirst(user.tipo)}</strong>.</p>`;
    header.append(title);

    const logoutButton = document.createElement('button');
    logoutButton.className = 'button button-secondary';
    logoutButton.textContent = 'Cerrar sesión';
    logoutButton.addEventListener('click', onLogout);
    header.append(logoutButton);

    container.append(header);

    const dashboard = document.createElement('div');
    dashboard.className = 'dashboard-grid';

    const content = document.createElement('div');
    content.className = 'card';

    if (user.tipo === 'profesor') {
        content.innerHTML = renderTeacherDashboard(classroomData);
    } else if (user.tipo === 'estudiante') {
        content.innerHTML = renderStudentDashboard(classroomData);
    } else if (user.tipo === 'apoderado') {
        content.innerHTML = renderGuardianDashboard(classroomData);
    } else {
        content.innerHTML = '<h2>Panel General</h2><p>Bienvenido a la plataforma.</p>';
    }

    dashboard.append(content);
    container.append(dashboard);
    return container;
}

function capitalizeFirst(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

function renderTeacherDashboard(data) {
    return `
        <h2>Panel de Profesor</h2>
        <h3>Cursos a cargo</h3>
        <ul>
            ${data.cursos?.map(c => `<li><strong>${c.nombre}</strong> - ${c.estudiantes} estudiantes | Promedio: ${c.promedio}</li>`).join('')}
        </ul>
        <h3>Evaluaciones pendientes</h3>
        <ul>
            ${data.evaluaciones?.map(e => `<li><strong>${e.nombre}</strong> (${e.fecha}) - ${e.estudiantes} estudiantes</li>`).join('')}
        </ul>
        <h3>Alertas</h3>
        ${data.alertas?.length ? `<ul>${data.alertas.map(a => `<li>${a.texto} <small>${a.fecha}</small></li>`).join('')}</ul>` : '<p>Sin alertas</p>'}
    `;
}

function renderStudentDashboard(data) {
    return `
        <h2>Panel de Estudiante</h2>
        <h3>Mis cursos</h3>
        <ul>
            ${data.cursos?.map(c => `<li><strong>${c.nombre}</strong> (${c.profesor}) - Calificación: <strong style="color: #2563eb">${c.calificacion}</strong></li>`).join('')}
        </ul>
        <h3>Tareas pendientes</h3>
        ${data.tareas?.filter(t => t.estado === 'pendiente').length ? `
            <ul>
                ${data.tareas.filter(t => t.estado === 'pendiente').map(t => `<li><strong>${t.titulo}</strong> (${t.asignatura}) - Entrega: ${t.fecha}</li>`).join('')}
            </ul>
        ` : '<p>Sin tareas pendientes</p>'}
        <h3>Asistencia</h3>
        <p>Presente: ${data.asistencia?.presente || 0} | Ausente: ${data.asistencia?.ausente || 0} | Justificada: ${data.asistencia?.justificada || 0}</p>
    `;
}

function renderGuardianDashboard(data) {
    return `
        <h2>Panel de Apoderado</h2>
        <h3>Mis hijos</h3>
        <ul>
            ${data.hijos?.map(h => `<li><strong>${h.nombre}</strong> - ${h.curso} | Promedio: ${h.promedio}</li>`).join('')}
        </ul>
        <h3>Alertas y notificaciones</h3>
        ${data.alertas?.length ? `
            <ul>
                ${data.alertas.map(a => `<li><span style="background: ${a.tipo === 'calificacion' ? '#fef2f2' : '#f0fdf4'}; padding: 4px 8px; border-radius: 4px;">${capitalizeFirst(a.tipo)}</span> ${a.texto} <small>${a.fecha}</small></li>`).join('')}
            </ul>
        ` : '<p>Sin alertas</p>'}
        <h3>Comunicados del colegio</h3>
        ${data.comunicados?.length ? `
            <ul>
                ${data.comunicados.map(c => `<li><strong>${c.asunto}</strong> (${c.de}) - ${c.fecha}</li>`).join('')}
            </ul>
        ` : '<p>Sin comunicados</p>'}
    `;
}

function renderApp(currentState) {
    const appRoot = document.getElementById('app');
    appRoot.innerHTML = '';

    if (currentState.error) {
        appRoot.append(renderAlert(currentState.error, 'error'));
    }
    if (currentState.message) {
        appRoot.append(renderAlert(currentState.message, 'success'));
    }

    if (currentState.view === 'login') {
        appRoot.append(loginContainer());
    } else if (currentState.view === 'register') {
        appRoot.append(registerContainer());
    } else if (currentState.view === 'dashboard' && currentState.user) {
        appRoot.append(dashboardContainer(currentState.user, currentState.classroomData));
    }

    const footer = document.createElement('footer');
    footer.textContent = '© 2026 Libro de Clases Digital - Auth Microservicio';
    appRoot.append(footer);
}

function renderAlert(text, type) {
    const alert = document.createElement('div');
    alert.className = `alert ${type === 'error' ? 'alert-error' : 'alert-success'}`;
    alert.textContent = text;
    return alert;
}

subscribe(renderApp);

if (restoreState()) {
    setState({ view: state.view || 'login' });
} else {
    setState({ view: 'login' });
}
