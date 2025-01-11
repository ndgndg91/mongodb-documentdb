import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10m', target: 100 }, // 10분 동안 100 VUs 유지
    ],
};

export default function () {
    const url = 'http://localhost:8080/api/notifications';

    const res = http.post(url);

    // 응답 상태 코드 확인
    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    // 약간의 지연 추가 (옵션, 일반적으로 필요 없음)
    sleep(0.1);
}
