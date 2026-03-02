/**
 * 完整测试脚本 - 创建所有测试数据
 *
 * 在浏览器控制台执行此脚本
 */

async function testAll() {
    console.log('=== 开始完整测试 ===\n');

    const API_BASE = '/api';

    // ============ 1. 创建用户 ============
    console.log('1. 创建用户...');
    const users = [
        { username: 'admin', email: 'admin@coffee.com', phone: '13800138001', role: 'ADMIN' },
        { username: 'staff1', email: 'staff1@coffee.com', phone: '13800138002', role: 'STAFF' },
        { username: 'staff2', email: 'staff2@coffee.com', phone: '13800138003', role: 'STAFF' },
        { username: 'customer1', email: 'customer1@coffee.com', phone: '13800138004', role: 'CUSTOMER' },
        { username: 'customer2', email: 'customer2@coffee.com', phone: '13800138005', role: 'CUSTOMER' }
    ];

    for (const user of users) {
        try {
            await fetch(API_BASE + '/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(user)
            });
            console.log('  ✓', user.username);
        } catch (e) {
            console.log('  ✗', user.username, e.message);
        }
    }

    // ============ 2. 创建产品 ============
    console.log('\n2. 创建产品...');
    const products = [
        { name: '拿铁咖啡', category: '咖啡', price: 28.00, stock: 100 },
        { name: '美式咖啡', category: '咖啡', price: 22.00, stock: 150 },
        { name: '卡布奇诺', category: '咖啡', price: 30.00, stock: 80 },
        { name: '抹茶拿铁', category: '特饮', price: 32.00, stock: 60 },
        { name: '焦糖玛奇朵', category: '咖啡', price: 35.00, stock: 50 }
    ];

    for (const product of products) {
        try {
            await fetch(API_BASE + '/products', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(product)
            });
            console.log('  ✓', product.name);
        } catch (e) {
            console.log('  ✗', product.name, e.message);
        }
    }

    // 等待一下，确保用户和产品创建完成
    await new Promise(resolve => setTimeout(resolve, 500));

    // ============ 3. 创建订单 ============
    console.log('\n3. 创建订单...');
    const orders = [
        {
            orderId: 'ORD-001',
            userId: 1,
            productId: 1,
            productName: '拿铁咖啡',
            sweetness: 50,
            iceLevel: 70,
            quantity: 1,
            unitPrice: 28.00,
            totalPrice: 28.00,
            remark: '少糖少冰'
        },
        {
            orderId: 'ORD-002',
            userId: 4,
            productId: 2,
            productName: '美式咖啡',
            sweetness: 0,
            iceLevel: 50,
            quantity: 2,
            unitPrice: 22.00,
            totalPrice: 44.00,
            remark: '无糖，常温'
        },
        {
            orderId: 'ORD-003',
            userId: 5,
            productId: 3,
            productName: '卡布奇诺',
            sweetness: 70,
            iceLevel: 30,
            quantity: 1,
            unitPrice: 30.00,
            totalPrice: 30.00,
            remark: '标准糖'
        },
        {
            orderId: 'ORD-004',
            userId: 4,
            productId: 4,
            productName: '抹茶拿铁',
            sweetness: 50,
            iceLevel: 50,
            quantity: 2,
            unitPrice: 32.00,
            totalPrice: 64.00,
            remark: '中糖中冰'
        },
        {
            orderId: 'ORD-005',
            userId: 5,
            productId: 5,
            productName: '焦糖玛奇朵',
            sweetness: 80,
            iceLevel: 20,
            quantity: 1,
            unitPrice: 35.00,
            totalPrice: 35.00,
            remark: '多糖少冰'
        }
    ];

    for (const order of orders) {
        try {
            await fetch(API_BASE + '/orders', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(order)
            });
            console.log('  ✓', order.orderId);
        } catch (e) {
            console.log('  ✗', order.orderId, e.message);
        }
    }

    // 等待一下
    await new Promise(resolve => setTimeout(resolve, 500));

    // ============ 4. 创建反馈 ============
    console.log('\n4. 创建反馈...');
    const feedbacks = [
        { userId: 4, orderId: 2, rating: 5, comment: '非常好喝，下次再来！' },
        { userId: 5, orderId: 3, rating: 4, comment: '味道不错，就是有点甜' },
        { userId: 4, orderId: 4, rating: 5, comment: '抹茶拿铁很香，推荐' },
        { userId: 5, orderId: 5, rating: 3, comment: '还行，可以改进' }
    ];

    for (const fb of feedbacks) {
        try {
            await fetch(API_BASE + '/feedback', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(fb)
            });
            console.log('  ✓ 用户', fb.userId, '的反馈');
        } catch (e) {
            console.log('  ✗ 反馈创建失败', e.message);
        }
    }

    // ============ 5. 验证数据 ============
    console.log('\n5. 验证数据...');

    try {
        const usersRes = await fetch(API_BASE + '/users');
        const usersData = await usersRes.json();
        console.log('  用户数量:', usersData.length);
    } catch (e) {
        console.log('  ✗ 获取用户失败');
    }

    try {
        const productsRes = await fetch(API_BASE + '/products');
        const productsData = await productsRes.json();
        console.log('  产品数量:', productsData.length);
    } catch (e) {
        console.log('  ✗ 获取产品失败');
    }

    try {
        const ordersRes = await fetch(API_BASE + '/orders');
        const ordersData = await ordersRes.json();
        console.log('  订单数量:', ordersData.length);
    } catch (e) {
        console.log('  ✗ 获取订单失败');
    }

    try {
        const feedbackRes = await fetch(API_BASE + '/feedback');
        const feedbackData = await feedbackRes.json();
        console.log('  反馈数量:', feedbackData.length);
    } catch (e) {
        console.log('  ✗ 获取反馈失败');
    }

    console.log('\n=== 测试完成 ===');
    console.log('刷新页面查看数据！');
    console.log('如果数据还是没有显示，请检查数据库表结构。');
}

// 执行测试
testAll();
