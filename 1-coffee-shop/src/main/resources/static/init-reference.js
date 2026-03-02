/**
 * 初始化 reference 表数据
 *
 * 在浏览器控制台执行此脚本来初始化甜度和冰度选项
 */

async function initReferenceData() {
    console.log('开始初始化 reference 数据...');

    const references = [
        // 甜度选项
        { refType: 'sweetness', refKey: 'NO_SUGAR', refValue: '无糖', refCode: 0, sortOrder: 1, isActive: 1 },
        { refType: 'sweetness', refKey: 'LOW_SUGAR', refValue: '少糖', refCode: 30, sortOrder: 2, isActive: 1 },
        { refType: 'sweetness', refKey: 'MEDIUM_SUGAR', refValue: '中糖', refCode: 50, sortOrder: 3, isActive: 1 },
        { refType: 'sweetness', refKey: 'HIGH_SUGAR', refValue: '多糖', refCode: 70, sortOrder: 4, isActive: 1 },
        { refType: 'sweetness', refKey: 'VERY_HIGH_SUGAR', refValue: '超多糖', refCode: 100, sortOrder: 5, isActive: 1 },

        // 冰度选项
        { refType: 'ice_level', refKey: 'HOT', refValue: '热饮', refCode: 0, sortOrder: 1, isActive: 1 },
        { refType: 'ice_level', refKey: 'LOW_ICE', refValue: '少冰', refCode: 30, sortOrder: 2, isActive: 1 },
        { refType: 'ice_level', refKey: 'MEDIUM_ICE', refValue: '中冰', refCode: 50, sortOrder: 3, isActive: 1 },
        { refType: 'ice_level', refKey: 'HIGH_ICE', refValue: '多冰', refCode: 70, sortOrder: 4, isActive: 1 },
        { refType: 'ice_level', refKey: 'VERY_HIGH_ICE', refValue: '超多冰', refCode: 100, sortOrder: 5, isActive: 1 },

        // 反馈类型
        { refType: 'feedback_type', refKey: 'PRODUCT_SUGGESTION', refValue: '产品建议', refCode: 1, sortOrder: 1, isActive: 1 },
        { refType: 'feedback_type', refKey: 'SERVICE_FEEDBACK', refValue: '服务反馈', refCode: 2, sortOrder: 2, isActive: 1 },
        { refType: 'feedback_type', refKey: 'COMPLAINT', refValue: '投诉', refCode: 3, sortOrder: 3, isActive: 1 },
        { refType: 'feedback_type', refKey: 'OTHER', refValue: '其他', refCode: 4, sortOrder: 4, isActive: 1 }
    ];

    let successCount = 0;
    let failCount = 0;

    for (const ref of references) {
        try {
            const response = await fetch('/api/reference', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(ref)
            });

            const result = await response.json();

            if (response.ok) {
                console.log('✓ 创建配置:', ref.refKey, '-', ref.refValue);
                successCount++;
            } else {
                console.log('✗ 创建配置失败:', ref.refKey, result.message);
                failCount++;
            }
        } catch (error) {
            console.error('✗ 创建配置异常:', ref.refKey, error.message);
            failCount++;
        }
    }

    console.log('\n初始化完成！');
    console.log('成功:', successCount, '失败:', failCount);
    console.log('刷新页面查看甜度和冰度下拉选项。');

    if (failCount > 0) {
        console.log('\n失败可能的原因：');
        console.log('1. 数据库表 `reference` 不存在');
        console.log('2. 数据库连接失败');
        console.log('3. 后端 ReferenceController 未正常工作');
    }

    console.log('\n验证数据：');
    try {
        const sweetnessRes = await fetch('/api/reference/type/sweetness');
        const sweetnessData = await sweetnessRes.json();
        console.log('甜度选项数量:', sweetnessData.length);

        const iceLevelRes = await fetch('/api/reference/type/ice_level');
        const iceLevelData = await iceLevelRes.json();
        console.log('冰度选项数量:', iceLevelData.length);
    } catch (e) {
        console.error('验证失败:', e.message);
    }
}

// 执行初始化
initReferenceData();
