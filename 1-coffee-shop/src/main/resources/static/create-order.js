/**
 * 创建测试订单脚本
 *
 * 在浏览器控制台执行此脚本来创建测试订单
 */

async function createTestOrders() {
    console.log('开始创建测试订单...');

    // 创建几个测试订单
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

    let successCount = 0;
    let failCount = 0;

    for (const order of orders) {
        try {
            const response = await fetch('/api/orders', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(order)
            });

            const result = await response.json();

            if (response.ok) {
                console.log('✓ 创建订单成功:', order.orderId);
                successCount++;
            } else {
                console.log('✗ 创建订单失败:', order.orderId, result.message);
                failCount++;
            }
        } catch (error) {
            console.error('✗ 创建订单异常:', order.orderId, error.message);
            failCount++;
        }
    }

    console.log('\n创建完成！');
    console.log('成功:', successCount, '失败:', failCount);
    console.log('刷新页面查看订单数据。');

    if (failCount > 0) {
        console.log('\n失败可能的原因：');
        console.log('1. 数据库表 `order` 不存在');
        console.log('2. 用户 ID 或产品 ID 不存在（需要先创建用户和产品）');
        console.log('3. 数据库连接失败');
    }
}

// 执行创建订单
createTestOrders();
