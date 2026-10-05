const API_BASE = '/api';

// DOM Elements
const loginContainer = document.getElementById('login-container');
const adminContainer = document.getElementById('admin-container');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const logoutBtn = document.getElementById('logout-btn');
const navItems = document.querySelectorAll('.nav-item');
const pageTitle = document.getElementById('page-title');
const detailModal = document.getElementById('detail-modal');
const detailModalTitle = document.getElementById('detail-modal-title');
const detailModalBody = document.getElementById('detail-modal-body');
const detailModalActions = document.getElementById('detail-modal-actions');
const detailModalClose = document.getElementById('detail-modal-close');
const detailModalDone = document.getElementById('detail-modal-done');
let currentPage = 'dashboard';

// Dashboard Stats Elements
const statUsers = document.getElementById('stat-users');
const statDrivers = document.getElementById('stat-drivers');
const statRestaurants = document.getElementById('stat-restaurants');
const statFoods = document.getElementById('stat-foods');
const statOrders = document.getElementById('stat-orders');
const dashboardOrdersBody = document.getElementById('orders-body');

// Orders Page Elements
const allOrdersBody = document.getElementById('all-orders-body');
const orderSearchInput = document.getElementById('order-search');
const orderSearchBtn = document.getElementById('order-search-btn');
const orderStatusFilter = document.getElementById('order-status-filter');

// Drivers Page Elements
const allDriversBody = document.getElementById('all-drivers-body');
const driverSearchInput = document.getElementById('driver-search');
const driverSearchBtn = document.getElementById('driver-search-btn');

// Users Page Elements
const allUsersBody = document.getElementById('all-users-body');
const userSearchInput = document.getElementById('user-search');
const userSearchBtn = document.getElementById('user-search-btn');

// Restaurants Page Elements
const allRestaurantsBody = document.getElementById('all-restaurants-body');
const restaurantSearchInput = document.getElementById('restaurant-search');
const restaurantSearchBtn = document.getElementById('restaurant-search-btn');
const addRestaurantBtn = document.getElementById('add-restaurant-btn');

// Foods Page Elements
const allFoodsBody = document.getElementById('all-foods-table') ? document.getElementById('all-foods-table').tBodies[0] : null;
const foodSearchInput = document.getElementById('food-search');
const foodSearchBtn = document.getElementById('food-search-btn');
const foodRestFilter = document.getElementById('food-restaurant-filter');
const addFoodBtn = document.getElementById('add-food-btn');

// Form modal elements
const restaurantModal = document.getElementById('restaurant-modal');
const restaurantForm = document.getElementById('restaurant-form');
const restModalTitle = document.getElementById('restaurant-modal-title');
const restCloseBtn = document.getElementById('restaurant-modal-close');
const deleteRestBtn = document.getElementById('delete-rest-btn');

const foodModal = document.getElementById('food-modal');
const foodForm = document.getElementById('food-form');
const foodModalTitle = document.getElementById('food-modal-title');
const foodCloseBtn = document.getElementById('food-modal-close');
const deleteFoodBtn = document.getElementById('delete-food-btn');

// Check initial session
window.onload = () => {
    const token = localStorage.getItem('admin_token');
    if (token) {
        showAdminPanel();
    }
};

// Login Handler
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        loginError.style.display = 'none';
        const btn = document.getElementById('login-btn');
        btn.disabled = true;
        btn.textContent = 'Loading...';

        try {
            const formData = new URLSearchParams();
            formData.append('username', email);
            formData.append('password', password);

            const response = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                body: formData
            });

            const data = await response.json();

            if (response.ok) {
                const token = data.access_token;
                const profileRes = await fetch(`${API_BASE}/users/me/`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                const profile = await profileRes.json();

                if (profile.role === 'ADMIN') {
                    localStorage.setItem('admin_token', token);
                    localStorage.setItem('admin_name', profile.full_name);
                    showAdminPanel();
                } else {
                    loginError.textContent = 'Access denied. You are not an administrator.';
                    loginError.style.display = 'block';
                }
            } else {
                loginError.textContent = data.detail || 'Login failed';
                loginError.style.display = 'block';
            }
        } catch (err) {
            loginError.textContent = 'Network error. Please try again.';
            loginError.style.display = 'block';
        } finally {
            btn.disabled = false;
            btn.textContent = 'Login';
        }
    });
}

// Logout Handler
if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
        localStorage.removeItem('admin_token');
        localStorage.removeItem('admin_name');
        location.reload();
    });
}

// Navigation
function navigateToPage(page) {
    currentPage = page;
    const navItem = [...navItems].find(item => item.dataset.page === page);
    navItems.forEach(item => item.classList.toggle('active', item.dataset.page === page));
    pageTitle.textContent = navItem ? navItem.textContent : 'Dashboard';
    document.querySelectorAll('.page-section').forEach(section => section.style.display = 'none');
    const section = document.getElementById(`${page}-page`) || document.getElementById('other-page');
    section.style.display = 'block';

    if (page === 'dashboard') fetchDashboard();
    else if (page === 'orders') fetchOrders();
    else if (page === 'drivers') fetchDrivers();
    else if (page === 'users') fetchUsers();
    else if (page === 'restaurants') fetchRestaurants();
    else if (page === 'foods') {
        fetchFoods();
        populateRestaurantFilter();
    }
}

function showDetailModal(title, content, actions = '') {
    detailModalTitle.textContent = title;
    detailModalBody.innerHTML = content;
    detailModalActions.innerHTML = actions;
    detailModal.style.display = 'flex';
    detailModal.setAttribute('aria-hidden', 'false');
    document.body.classList.add('modal-open');
}

navItems.forEach(item => item.addEventListener('click', event => {
    event.preventDefault();
    navigateToPage(item.dataset.page);
}));
function closeDetailModal() {
    detailModal.style.display = 'none';
    detailModal.setAttribute('aria-hidden', 'true');
    document.body.classList.remove('modal-open');
}
if (detailModalClose) detailModalClose.onclick = closeDetailModal;
if (detailModalDone) detailModalDone.onclick = closeDetailModal;

// Filtering
if (orderSearchBtn) orderSearchBtn.onclick = fetchOrders;
if (orderStatusFilter) orderStatusFilter.onchange = fetchOrders;
if (driverSearchBtn) driverSearchBtn.onclick = fetchDrivers;
if (userSearchBtn) userSearchBtn.onclick = fetchUsers;
if (restaurantSearchBtn) restaurantSearchBtn.onclick = fetchRestaurants;
if (foodSearchBtn) foodSearchBtn.onclick = fetchFoods;
if (foodRestFilter) foodRestFilter.onchange = fetchFoods;

// Form modal closing
if (restCloseBtn) restCloseBtn.onclick = () => restaurantModal.style.display = 'none';
if (foodCloseBtn) foodCloseBtn.onclick = () => foodModal.style.display = 'none';

window.onclick = (event) => {
    if (event.target === detailModal) closeDetailModal();
    if (event.target == restaurantModal) restaurantModal.style.display = 'none';
    if (event.target == foodModal) foodModal.style.display = 'none';
};

// Data Fetching Functions
function showAdminPanel() {
    loginContainer.style.display = 'none';
    adminContainer.style.display = 'flex';
    document.getElementById('admin-name').textContent = localStorage.getItem('admin_name');
    fetchDashboard();
}

async function fetchDashboard() {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/dashboard`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.status === 401 || response.status === 403) {
            localStorage.removeItem('admin_token');
            location.reload();
            return;
        }

        const data = await response.json();

        statUsers.textContent = data.total_users;
        statDrivers.textContent = data.total_drivers;
        statRestaurants.textContent = data.total_restaurants;
        statFoods.textContent = data.total_foods;
        statOrders.textContent = data.total_orders;

        dashboardOrdersBody.innerHTML = '';
        data.recent_orders.forEach(order => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${order.id}</td>
                <td>User #${order.user_id}</td>
                <td>${formatPrice(order.total_amount)} VND</td>
                <td>${order.payment_method}</td>
                <td><span class="status-badge">${order.status}</span></td>
                <td>${formatTime(order.created_at)}</td>
            `;
            dashboardOrdersBody.appendChild(row);
        });
    } catch (err) {
        console.error('Failed to fetch dashboard', err);
    }
}

async function fetchOrders() {
    const token = localStorage.getItem('admin_token');
    const status = orderStatusFilter.value;
    const search = orderSearchInput.value;

    let url = `${API_BASE}/admin/orders?`;
    if (status) url += `status=${encodeURIComponent(status)}&`;
    if (search) url += `search=${encodeURIComponent(search)}&`;

    try {
        const response = await fetch(url, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();

        allOrdersBody.innerHTML = data.length ? '' : '<tr><td colspan="7" class="empty-state">No orders found.</td></tr>';
        data.forEach(order => {
            const row = document.createElement('tr');
            const customerName = order.user ? order.user.full_name : `User #${order.user_id}`;
            row.innerHTML = `
                <td>#${order.id}</td>
                <td>${escapeHtml(customerName)}</td>
                <td>${formatPrice(order.total_amount)} VND</td>
                <td>${escapeHtml(order.payment_method || 'N/A')}</td>
                <td><span class="status-badge">${order.status}</span></td>
                <td>${formatTime(order.created_at)}</td>
        <td><div class="table-actions"><button class="view-btn" onclick="viewOrderDetail(${order.id})">View</button></div></td>
            `;
            allOrdersBody.appendChild(row);
        });
    } catch (err) {
        console.error('Failed to fetch orders', err);
    }
}

async function viewOrderDetail(orderId) {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/orders/${orderId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const order = await response.json();

        const customer = order.user || {};
        const address = order.address || {};
        const payment = order.payment || {};

        let itemsHtml = (order.items || []).map(item => `
            <tr>
                <td>${escapeHtml(item.food ? item.food.name : 'Unknown')}</td>
                <td>${item.quantity}</td>
                <td>${formatPrice(item.price_snapshot)} VND</td>
                <td>${formatPrice(item.quantity * item.price_snapshot)} VND</td>
            </tr>
        `).join('');

        showDetailModal(`Order Details #${order.id}`, `
            <div class="detail-section">
                <h3>Order Information</h3>
                <div class="detail-grid">
                    <div class="detail-item"><label>Order ID</label><span>#${order.id}</span></div>
                    <div class="detail-item"><label>Status</label><span class="status-badge">${escapeHtml(order.status)}</span></div>
                    <div class="detail-item"><label>Time</label><span>${formatTime(order.created_at)}</span></div>
                    <div class="detail-item"><label>Payment Method</label><span>${escapeHtml(payment.method || order.payment_method || 'N/A')}</span></div>
                </div>
            </div>

            <div class="detail-section">
                <h3>Customer Information</h3>
                <div class="detail-grid">
                    <div class="detail-item"><label>Name</label><span>${escapeHtml(customer.full_name || 'N/A')}</span></div>
                    <div class="detail-item"><label>Email</label><span>${escapeHtml(customer.email || 'N/A')}</span></div>
                    <div class="detail-item"><label>Recipient</label><span>${escapeHtml(address.recipient_name || 'N/A')}</span></div>
                    <div class="detail-item"><label>Phone</label><span>${escapeHtml(address.phone || 'N/A')}</span></div>
                    <div class="detail-item full-width"><label>Address</label><span>${escapeHtml(address.address_line || 'N/A')}</span></div>
                </div>
            </div>

            <div class="detail-section">
                <h3>Order Items</h3>
                <div class="table-container detail-table-container"><table style="width: 100%; font-size: 14px;">
                    <thead>
                        <tr>
                            <th>Item</th>
                            <th>Qty</th>
                            <th>Price</th>
                            <th>Subtotal</th>
                        </tr>
                    </thead>
                    <tbody>${itemsHtml}</tbody>
                    <tfoot>
                        <tr>
                            <td colspan="3" style="text-align: right; font-weight: 700; padding: 12px;">Total:</td>
                            <td style="font-weight: 700; color: var(--primary-color); padding: 12px;">${formatPrice(order.total_amount)} VND</td>
                        </tr>
                    </tfoot>
                </table></div>
            </div>

            <div class="detail-section">
                <h3>Payment Details</h3>
                <div class="detail-grid">
                    <div class="detail-item"><label>Payment Status</label><span>${escapeHtml(payment.status || 'N/A')}</span></div>
                    <div class="detail-item"><label>Payment Amount</label><span>${payment.amount != null ? `${formatPrice(payment.amount)} VND` : 'N/A'}</span></div>
                    <div class="detail-item"><label>Transaction Code</label><span>${escapeHtml(payment.transaction_code || 'N/A')}</span></div>
                </div>
            </div>
        `);
    } catch (err) {
        console.error('Failed to fetch order detail', err);
    }
}

async function fetchDrivers() {
    const token = localStorage.getItem('admin_token');
    const search = driverSearchInput.value;

    let url = `${API_BASE}/admin/drivers?`;
    if (search) url += `search=${search}&`;

    try {
        const response = await fetch(url, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();

        allDriversBody.innerHTML = data.length ? '' : '<tr><td colspan="7" class="empty-state">No drivers found.</td></tr>';
        data.forEach(driver => {
            const row = document.createElement('tr');
            const statusClass = driver.is_active ? 'active-status' : 'inactive-status';
            const statusLabel = driver.is_active ? 'Active' : 'Inactive';
            const currentOrder = driver.current_order_id ? `#${driver.current_order_id}` : 'None';

            row.innerHTML = `
                <td>#${driver.id}</td>
                <td>${escapeHtml(driver.full_name || 'N/A')}</td>
                <td>${escapeHtml(driver.email)}</td>
                <td><span class="status-badge ${statusClass}">${statusLabel}</span></td>
                <td>${driver.total_completed_orders}</td>
                <td>${currentOrder}</td>
                <td><div class="table-actions"><button class="view-btn" onclick="viewDriverDetail(${driver.id})">View</button></div></td>
            `;
            allDriversBody.appendChild(row);
        });
    } catch (err) {
        console.error('Failed to fetch drivers', err);
    }
}

async function viewDriverDetail(driverId) {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/drivers/${driverId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const detail = await response.json();

        const user = detail.user;
        const activeOrder = detail.active_order;

        const statusBtnLabel = user.is_active ? 'Deactivate Driver' : 'Activate Driver';
        const statusBtnClass = user.is_active ? 'deactivate-btn' : 'activate-btn';

        let activeOrderHtml = activeOrder ? `
            <div class="active-order-box">
                <div class="detail-grid">
                    <div class="detail-item"><label>Order ID</label><span>#${activeOrder.id}</span></div>
                    <div class="detail-item"><label>Status</label><span class="status-badge">${activeOrder.status}</span></div>
                    <div class="detail-item"><label>Total</label><span>${formatPrice(activeOrder.total_amount)} VND</span></div>
                    <div class="detail-item"><label>Customer ID</label><span>#${activeOrder.user_id}</span></div>
                </div>
            </div>
        ` : '<p>No active delivery</p>';

        const content = `
            <div class="detail-section">
                <h3>Driver Profile</h3>
                <div class="detail-grid">
                    <div class="detail-item"><label>ID</label><span>#${user.id}</span></div>
                    <div class="detail-item"><label>Status</label><span class="status-badge ${user.is_active ? 'active-status' : 'inactive-status'}">${user.is_active ? 'Active' : 'Inactive'}</span></div>
                    <div class="detail-item"><label>Full Name</label><span>${escapeHtml(user.full_name || 'N/A')}</span></div>
                    <div class="detail-item"><label>Email</label><span>${escapeHtml(user.email)}</span></div>
                    <div class="detail-item"><label>Created At</label><span>${formatTime(user.created_at)}</span></div>
                </div>
            </div>

            <div class="detail-section">
                <h3>Statistics</h3>
                <div class="stats-mini-grid">
                    <div class="stat-mini-card">
                        <label>Total Orders</label>
                        <span>${detail.total_orders}</span>
                    </div>
                    <div class="stat-mini-card">
                        <label>Completed</label>
                        <span>${detail.completed_orders}</span>
                    </div>
                    <div class="stat-mini-card">
                        <label>Cancelled</label>
                        <span>${detail.cancelled_orders}</span>
                    </div>
                </div>
            </div>

            <div class="detail-section">
                <h3>Active Delivery</h3>
                ${activeOrderHtml}
            </div>

            <div class="action-section">
                <button class="status-toggle-btn ${statusBtnClass}" onclick="toggleDriverStatus(${user.id})">
                    ${statusBtnLabel}
                </button>
            </div>
        `;

        showDetailModal('Driver Details', content, `<button class="status-toggle-btn ${statusBtnClass}" onclick="toggleDriverStatus(${user.id})">${statusBtnLabel}</button>`);
    } catch (err) {
        console.error('Failed to fetch driver detail', err);
    }
}

async function toggleDriverStatus(driverId) {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/drivers/${driverId}/status`, {
            method: 'PATCH',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        const data = await response.json();

        if (response.ok) {
            viewDriverDetail(driverId);
            fetchDrivers();
        } else {
            alert(data.detail || 'Failed to update driver status');
        }
    } catch (err) {
        alert('Network error. Please try again.');
    }
}

async function fetchUsers() {
    const token = localStorage.getItem('admin_token');
    const search = userSearchInput.value;

    let url = `${API_BASE}/admin/users?`;
    if (search) url += `search=${encodeURIComponent(search)}&`;

    try {
        const response = await fetch(url, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();

        allUsersBody.innerHTML = data.length ? '' : '<tr><td colspan="7" class="empty-state">No users found.</td></tr>';
        data.forEach(user => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${user.id}</td>
                <td>${escapeHtml(user.full_name || 'N/A')}</td>
                <td>${escapeHtml(user.email)}</td>
                <td>Customer</td>
                <td><span class="status-badge ${user.is_active ? 'active-status' : 'inactive-status'}">${user.is_active ? 'Active' : 'Inactive'}</span></td>
                <td>${formatTime(user.created_at)}</td>
                <td><div class="table-actions"><button class="view-btn" onclick="viewUserDetail(${user.id})">View</button></div></td>
            `;
            allUsersBody.appendChild(row);
        });
    } catch (err) {
        console.error('Failed to fetch users', err);
    }
}

async function viewUserDetail(userId) {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/users/${userId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const detail = await response.json();

        const user = detail.user;
        const content = `
            <div class="detail-section">
                <h3>User Profile</h3>
                <div class="detail-grid">
                    <div class="detail-item"><label>ID</label><span>#${user.id}</span></div>
                    <div class="detail-item"><label>Role</label><span>Customer</span></div>
                    <div class="detail-item"><label>Status</label><span class="status-badge ${user.is_active ? 'active-status' : 'inactive-status'}">${user.is_active ? 'Active' : 'Inactive'}</span></div>
                    <div class="detail-item"><label>Full Name</label><span>${escapeHtml(user.full_name || 'N/A')}</span></div>
                    <div class="detail-item"><label>Email</label><span>${escapeHtml(user.email)}</span></div>
                    <div class="detail-item"><label>Created At</label><span>${formatTime(user.created_at)}</span></div>
                </div>
            </div>

        `;

        showDetailModal('User Details', content);
    } catch (err) {
        console.error('Failed to fetch user detail', err);
    }
}

async function fetchRestaurants() {
    const token = localStorage.getItem('admin_token');
    const search = restaurantSearchInput.value;
    let url = `${API_BASE}/admin/restaurants?`;
    if (search) url += `search=${encodeURIComponent(search)}&`;

    try {
        const response = await fetch(url, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();

        allRestaurantsBody.innerHTML = data.length ? '' : '<tr><td colspan="6" class="empty-state">No restaurants found.</td></tr>';
        data.forEach(r => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>#${r.id}</td>
                <td>${escapeHtml(r.name)}</td>
                <td>${escapeHtml(r.address)}</td>
                <td>${escapeHtml(r.open_hours || 'N/A')}</td>
                <td>${r.food_count}</td>
                <td><div class="table-actions">
                    <button class="view-btn" onclick="viewRestaurantDetail(${r.id})">View</button>
                    <button class="edit-btn" onclick="openEditRestaurantModal(${r.id})">Edit</button>
                    <button class="delete-row-btn" onclick="deleteRestaurant(${r.id})" ${r.can_delete ? '' : 'disabled title="Cannot delete because this item is used in order or review history."'}>Delete</button>
                </div></td>
            `;
            allRestaurantsBody.appendChild(row);
        });
    } catch (err) {
        console.error('Failed to fetch restaurants', err);
    }
}

async function viewRestaurantDetail(id) {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/restaurants/${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const r = await response.json();

        const content = `
            <div class="detail-section">
                <h3>General Info</h3>
                <div class="detail-grid">
                    <div class="detail-item"><label>ID</label><span>#${r.id}</span></div>
                    <div class="detail-item"><label>Name</label><span>${escapeHtml(r.name)}</span></div>
                    <div class="detail-item full-width"><label>Address</label><span>${escapeHtml(r.address)}</span></div>
                    <div class="detail-item"><label>Open Hours</label><span>${escapeHtml(r.open_hours || 'N/A')}</span></div>
                    <div class="detail-item"><label>Created At</label><span>${formatTime(r.created_at)}</span></div>
                </div>
            </div>
            <div class="detail-section">
                <h3>Asset Keys</h3>
                <div class="detail-grid">
                    <div class="detail-item"><label>Logo Key</label><span>${escapeHtml(r.logo_url || 'None')}</span></div>
                    <div class="detail-item"><label>Cover Key</label><span>${escapeHtml(r.cover_url || 'None')}</span></div>
                </div>
            </div>
            <div class="detail-section">
                <h3>Statistics</h3>
                <div class="stats-mini-grid">
                    <div class="stat-mini-card"><label>Foods</label><span>${r.food_count}</span></div>
                    <div class="stat-mini-card"><label>Categories</label><span>${r.category_count}</span></div>
                    <div class="stat-mini-card"><label>Orders</label><span>${r.total_orders}</span></div>
                </div>
            </div>
        `;
        const actions = `<button class="edit-btn" onclick="closeDetailModal(); openEditRestaurantModal(${r.id})">Edit</button><button class="delete-row-btn" onclick="deleteRestaurant(${r.id})" ${r.can_delete ? '' : 'disabled title="Cannot delete because this item is used in order or review history."'}>Delete</button>`;
        showDetailModal('Restaurant Details', content, actions);
    } catch (err) {
        console.error('Detail fetch failed', err);
    }
}

async function openEditRestaurantModal(id) {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/restaurants/${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const r = await response.json();

        restModalTitle.textContent = 'Edit Restaurant';
        document.getElementById('edit-restaurant-id').value = r.id;
        document.getElementById('rest-name').value = r.name;
        document.getElementById('rest-address').value = r.address;
        document.getElementById('rest-hours').value = r.open_hours || '';
        document.getElementById('rest-logo').value = r.logo_url || '';
        document.getElementById('rest-cover').value = r.cover_url || '';

        deleteRestBtn.style.display = r.can_delete ? 'block' : 'none';
        deleteRestBtn.onclick = () => deleteRestaurant(r.id, true);
        restaurantModal.style.display = 'flex';
    } catch (err) {
        console.error('Fetch failed', err);
    }
}

async function fetchFoods() {
    const token = localStorage.getItem('admin_token');
    const restId = foodRestFilter.value;
    const search = foodSearchInput.value;

    let url = `${API_BASE}/admin/foods?`;
    if (restId) url += `restaurant_id=${encodeURIComponent(restId)}&`;
    if (search) url += `search=${encodeURIComponent(search)}&`;

    try {
        const response = await fetch(url, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();

        allFoodsBody.innerHTML = data.length ? '' : '<tr><td colspan="7" class="empty-state">No foods found.</td></tr>';
        data.forEach(f => {
            const row = document.createElement('tr');
            const statusClass = f.is_available ? 'active-status' : 'inactive-status';
            const statusLabel = f.is_available ? 'Available' : 'Unavailable';

            row.innerHTML = `
                <td>#${f.id}</td>
                <td>${escapeHtml(f.name)}</td>
                <td>${escapeHtml(f.restaurant_name)}</td>
                <td>${escapeHtml(f.category_name)}</td>
                <td>${formatPrice(f.price)} VND</td>
                <td><span class="status-badge ${statusClass}">${statusLabel}</span></td>
                <td><div class="table-actions">
                    <button class="view-btn" onclick="viewFoodDetail(${f.id})">View</button>
                    <button class="edit-btn" onclick="openEditFoodModal(${f.id})">Edit</button>
                    <button class="delete-row-btn" onclick="deleteFood(${f.id})" ${f.can_delete ? '' : 'disabled title="Cannot delete because this item is used in order or review history."'}>Delete</button>
                </div></td>
            `;
            allFoodsBody.appendChild(row);
        });
    } catch (err) {
        console.error('Failed to fetch foods', err);
    }
}

async function viewFoodDetail(id) {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/foods/${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const f = await response.json();
        const [restaurantsResponse, categoriesResponse] = await Promise.all([
            fetch(`${API_BASE}/admin/restaurants`, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch(`${API_BASE}/admin/categories?restaurant_id=${encodeURIComponent(f.restaurant_id)}`, { headers: { 'Authorization': `Bearer ${token}` } })
        ]);
        const restaurants = restaurantsResponse.ok ? await restaurantsResponse.json() : [];
        const categories = categoriesResponse.ok ? await categoriesResponse.json() : [];
        const restaurantName = restaurants.find(item => item.id === f.restaurant_id)?.name;
        const categoryName = categories.find(item => item.id === f.category_id)?.name;

        const content = `
            <div class="detail-section"><h3>Food Information</h3><div class="detail-grid">
                <div class="detail-item"><label>ID</label><span>#${f.id}</span></div>
                <div class="detail-item"><label>Name</label><span>${escapeHtml(f.name)}</span></div>
                <div class="detail-item"><label>Restaurant</label><span>${escapeHtml(restaurantName || (f.restaurant_id != null ? `#${f.restaurant_id}` : 'N/A'))}</span></div>
                <div class="detail-item"><label>Category</label><span>${escapeHtml(categoryName || (f.category_id != null ? `#${f.category_id}` : 'N/A'))}</span></div>
                <div class="detail-item"><label>Price</label><span>${formatPrice(f.price)} VND</span></div>
                <div class="detail-item"><label>Status</label><span class="status-badge ${f.is_available ? 'active-status' : 'inactive-status'}">${f.is_available ? 'Available' : 'Unavailable'}</span></div>
                ${f.image_url && /^https?:\/\//i.test(f.image_url) ? `<div class="detail-item full-width"><label>Image</label><img class="food-detail-image" src="${escapeHtml(f.image_url)}" alt="${escapeHtml(f.name)}"></div>` : ''}
                <div class="detail-item full-width"><label>Description</label><span>${escapeHtml(f.description || 'N/A')}</span></div>
                <div class="detail-item full-width"><label>Image Key</label><span>${escapeHtml(f.image_url || 'N/A')}</span></div>
            </div></div>`;
        const actions = `<button class="edit-btn" onclick="closeDetailModal(); openEditFoodModal(${f.id})">Edit</button><button class="delete-row-btn" onclick="deleteFood(${f.id})" ${f.can_delete ? '' : 'disabled title="Cannot delete because this item is used in order or review history."'}>Delete</button>`;
        showDetailModal('Food Details', content, actions);
    } catch (err) {
        console.error('Food detail fetch failed', err);
    }
}

async function openEditFoodModal(id) {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/foods/${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const f = await response.json();

        foodModalTitle.textContent = 'Edit Food Item';
        document.getElementById('edit-food-id').value = f.id;
        document.getElementById('food-name').value = f.name;
        document.getElementById('food-description').value = f.description || '';
        document.getElementById('food-price').value = f.price;
        document.getElementById('food-image').value = f.image_url || '';
        document.getElementById('food-available').checked = f.is_available;

        await populateRestaurantSelect('food-rest-id', f.restaurant_id);
        await populateCategorySelect(f.restaurant_id, 'food-cat-id', f.category_id);
        document.getElementById('food-cat-id').disabled = false;

        deleteFoodBtn.style.display = f.can_delete ? 'block' : 'none';
        deleteFoodBtn.onclick = () => deleteFood(f.id, true);
        foodModal.style.display = 'flex';
    } catch (err) {
        console.error('Food fetch failed', err);
    }
}

if (addRestaurantBtn) addRestaurantBtn.onclick = () => {
    restaurantForm.reset();
    document.getElementById('edit-restaurant-id').value = '';
    restModalTitle.textContent = 'Add Restaurant';
    deleteRestBtn.style.display = 'none';
    restaurantModal.style.display = 'flex';
};

if (restaurantForm) restaurantForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const id = document.getElementById('edit-restaurant-id').value;
    const token = localStorage.getItem('admin_token');
    const payload = {
        name: document.getElementById('rest-name').value.trim(),
        address: document.getElementById('rest-address').value.trim(),
        open_hours: document.getElementById('rest-hours').value.trim() || null,
        logo_url: document.getElementById('rest-logo').value.trim() || null,
        cover_url: document.getElementById('rest-cover').value.trim() || null
    };
    if (!payload.name || !payload.address) return alert('Name and address are required.');
    try {
        const response = await fetch(`${API_BASE}/admin/restaurants${id ? `/${id}` : ''}`, {
            method: id ? 'PUT' : 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        if (!response.ok) return alert(data.detail || 'Could not save restaurant.');
        restaurantModal.style.display = 'none';
        await fetchRestaurants();
        await populateRestaurantFilter();
    } catch (err) { alert('Network error. Please try again.'); }
});

async function deleteRestaurant(id, closeModal = false) {
    if (!confirm('Delete this restaurant and its categories and foods?')) return;
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/restaurants/${id}`, {
            method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        if (!response.ok) return alert(data.detail || 'Could not delete restaurant.');
        if (closeModal) restaurantModal.style.display = 'none';
        await fetchRestaurants();
        await populateRestaurantFilter();
    } catch (err) { alert('Network error. Please try again.'); }
}

async function deleteFood(id, closeModal = false) {
    if (!confirm('Delete this food? This is only allowed when it has no order or review history.')) return;
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/foods/${id}`, {
            method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        if (!response.ok) return alert(data.detail || 'Could not delete food.');
        if (closeModal) foodModal.style.display = 'none';
        await fetchFoods();
    } catch (err) { alert('Network error. Please try again.'); }
}

if (addFoodBtn) addFoodBtn.onclick = async () => {
    foodForm.reset();
    document.getElementById('edit-food-id').value = '';
    foodModalTitle.textContent = 'Add Food';
    deleteFoodBtn.style.display = 'none';
    document.getElementById('food-cat-id').innerHTML = '<option value="">Select a restaurant first</option>';
    document.getElementById('food-cat-id').disabled = true;
    await populateRestaurantSelect('food-rest-id');
    foodModal.style.display = 'flex';
};

const foodRestaurantSelect = document.getElementById('food-rest-id');
if (foodRestaurantSelect) foodRestaurantSelect.onchange = async () => {
    const categorySelect = document.getElementById('food-cat-id');
    if (!foodRestaurantSelect.value) {
        categorySelect.innerHTML = '<option value="">Select a restaurant first</option>';
        categorySelect.disabled = true;
        return;
    }
    categorySelect.disabled = false;
    await populateCategorySelect(foodRestaurantSelect.value, 'food-cat-id');
};

if (foodForm) foodForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const id = document.getElementById('edit-food-id').value;
    const restaurantId = document.getElementById('food-rest-id').value;
    const categoryId = document.getElementById('food-cat-id').value;
    const price = Number(document.getElementById('food-price').value);
    const payload = {
        name: document.getElementById('food-name').value.trim(),
        description: document.getElementById('food-description').value.trim() || null,
        price,
        image_url: document.getElementById('food-image').value.trim() || null,
        is_available: document.getElementById('food-available').checked
    };
    if (!payload.name || !Number.isFinite(price) || price <= 0 || !restaurantId || !categoryId) {
        return alert('Enter a name, positive price, restaurant, and category.');
    }
    const token = localStorage.getItem('admin_token');
    const params = new URLSearchParams({ restaurant_id: restaurantId, category_id: categoryId });
    try {
        const response = await fetch(`${API_BASE}/admin/foods${id ? `/${id}` : ''}?${params}`, {
            method: id ? 'PUT' : 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        if (!response.ok) return alert(data.detail || 'Could not save food.');
        foodModal.style.display = 'none';
        await fetchFoods();
    } catch (err) { alert('Network error. Please try again.'); }
});

async function populateRestaurantFilter() {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/restaurants`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        const filter = document.getElementById('food-restaurant-filter');
        if (!filter) return;
        const currentVal = filter.value;
        filter.innerHTML = '<option value="">All Restaurants</option>';
        data.forEach(r => {
            const option = document.createElement('option');
            option.value = r.id;
            option.textContent = r.name;
            filter.appendChild(option);
        });
        filter.value = currentVal;
    } catch (err) {}
}

async function populateRestaurantSelect(selectId, selectedId = null) {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/restaurants`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        const select = document.getElementById(selectId);
        if (!select) return;
        select.innerHTML = '<option value="">Select Restaurant</option>';
        data.forEach(r => {
            const option = document.createElement('option');
            option.value = r.id;
            option.textContent = r.name;
            option.selected = Boolean(selectedId && r.id == selectedId);
            select.appendChild(option);
        });
    } catch (err) {}
}

async function populateCategorySelect(restId, selectId, selectedId = null) {
    const token = localStorage.getItem('admin_token');
    try {
        const response = await fetch(`${API_BASE}/admin/categories?restaurant_id=${restId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        const select = document.getElementById(selectId);
        if (!select) return;
        select.innerHTML = '<option value="">Select Category</option>';
        data.forEach(c => {
            const option = document.createElement('option');
            option.value = c.id;
            option.textContent = c.name;
            option.selected = Boolean(selectedId && c.id == selectedId);
            select.appendChild(option);
        });
    } catch (err) {}
}

// Helpers
function formatPrice(val) {
    return new Intl.NumberFormat().format(val);
}

function formatTime(isoStr) {
    return new Date(isoStr).toLocaleString();
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, character => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[character]);
}

// Attach to window for onclick handlers in dynamic HTML
window.viewOrderDetail = viewOrderDetail;
window.viewDriverDetail = viewDriverDetail;
window.toggleDriverStatus = toggleDriverStatus;
window.viewUserDetail = viewUserDetail;
window.viewRestaurantDetail = viewRestaurantDetail;
window.openEditRestaurantModal = openEditRestaurantModal;
window.deleteRestaurant = deleteRestaurant;
window.viewFoodDetail = viewFoodDetail;
window.openEditFoodModal = openEditFoodModal;
window.deleteFood = deleteFood;
