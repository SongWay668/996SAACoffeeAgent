/**
 * 测试脚本：初始化测试数据
 *
 * 在浏览器控制台中执行此脚本来初始化测试数据
 */

async function initData() {
    console.log('开始初始化测试数据...');

    // 清空并创建测试用户
    const users = [
        { id: 1, username: 'admin', email: 'admin@coffee.com', phone: '13800138001', role: 'ADMIN' },
        { id: 2, username: 'staff1', email: 'staff1@coffee.com', phone: '13800138002', role: 'STAFF' },
        { id: 3, username: 'staff2', email: 'staff2@coffee.com', phone: '13800138003', role: 'STAFF' },
        { id: 4, username: 'customer1', email: 'customer1@coffee.com', phone: '13800138004', role: 'CUSTOMER' },
        { id: 5, username: 'customer2', email: 'customer2@coffee.com', phone: '13800138005', role: 'CUSTOMER' }
    ];

    for (const user of users) {
        try {
            await fetch('/api/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(user)
            });
            console.log('创建用户:', user.username);
        } catch (e) {
            console.error('创建用户失败:', user.username, e);
        }
    }

    // 创建测试产品
    const products = [
        { id: 1, name: '拿铁咖啡', category: '咖啡', price: 28.00, stock: 100 },
        { id: 2, name: '美式咖啡', category: '咖啡', price: 22.00, stock: 150 },
        { id: 3, name: '卡布奇诺', category: '咖啡', price: 30.00, stock: 80 },
        { id: 4, name: '抹茶拿铁', category: '特饮', price: 32.00, stock: 60 },
        { id: 5, name: '焦糖玛奇朵', category: '咖啡', price: 35.00, stock: 50 }
    ];

    for (const product of products) {
        try {
            await fetch('/api/products', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(product)
            });
            console.log('创建产品:', product.name);
        } catch (e) {
            console.error('创建产品失败:', product.name, e);
        }
    }

    console.log('初始化完成！刷新页面查看数据。');
    console.log('如果数据还是没有显示，请检查：');
    console.log('1. 后端是否正常启动（端口8080）');
    console.log('2. 数据库是否存在表结构');
    console.log('3. 浏览器控制台是否有错误');
}

// 执行初始化
initData();
