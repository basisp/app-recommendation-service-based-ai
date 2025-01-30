from flask import Flask, request, jsonify
import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.sequence import pad_sequences
import pandas as pd
from sklearn.preprocessing import LabelEncoder
import tensorflow as tf
from flask_sqlalchemy import SQLAlchemy
import json

# Flask 애플리케이션 초기화
app = Flask(__name__)

# MySQL 데이터베이스 설정

# SQLAlchemy 객체 생성
db = SQLAlchemy(app)

# 모델 정의
class YourTable(db.Model):
    __tablename__ = 'UserDataEntity'
    
    # 컬럼 정의
    allAppJson = db.Column(db.Text)  # MEDIUMTEXT에 해당
    username = db.Column(db.String(255), primary_key=True)  # VARCHAR(255)에 해당

# 모든 GPU 비활성화
tf.config.set_visible_devices([], 'GPU')

# 모델 파일 경로
lstm_model_file_path = 'lstm.h5'  # 저장한 LSTM 모델 파일 경로
gru_model_file_path = 'gru.h5'    # 저장한 GRU 모델 파일 경로
rnn_model_file_path = 'rnn.h5'    # 저장한 RNN 모델 파일 경로

# LSTM, GRU, RNN 모델 불러오기
lstm_model = load_model(lstm_model_file_path) 
gru_model = load_model(gru_model_file_path)
rnn_model = load_model(rnn_model_file_path)

# 패키지 이름과 카테고리 정보를 인코딩하기 위한 LabelEncoder 초기화
package_label_encoder = LabelEncoder()
category_label_encoder = LabelEncoder()

@app.route('/receive', methods=['POST'])
def predict():
    # 클라이언트에서 JSON 요청 받기
    data = request.json
    username = data.get('username')  # username 추출

    if not username:
        return "Username is required", 400

    # DB에서 해당 username의 데이터 찾기
    result = YourTable.query.filter_by(username=username).first()

    if not result:
        return "User not found", 404

    # allAppJson 데이터 확인
    all_app_data = result.allAppJson

    if not all_app_data:
        return "No app data found for the user", 404

    # all_app_data JSON 데이터를 DataFrame으로 변환
    df = pd.read_json(all_app_data)

    # 'apps' 열에서 package_name과 category 추출
    df_apps = pd.json_normalize(df['apps'])  # 'apps' 열을 분리
    df = pd.concat([df.drop(columns=['apps']), df_apps], axis=1)  # 원래 데이터와 분리된 데이터 병합

    # Label Encoding 적용
    package_label_encoder = LabelEncoder()
    category_label_encoder = LabelEncoder()

    df['package_encoded'] = package_label_encoder.fit_transform(df['package_name'])
    df['category_encoded'] = category_label_encoder.fit_transform(df['category'])

    sequence_data = data['apps']  # 'apps' 키에서 시퀀스 데이터를 추출

    # 시퀀스 데이터를 DataFrame으로 변환
    sequence_df = pd.json_normalize(sequence_data)
    sequence_df = sequence_df[['package_name', 'category', 'timestamp']]  # 시퀀스에 필요한 열 선택

    # 타임스탬프 전처리 (밀리초에서 초 단위 UNIX 시간으로 변환)
    sequence_df['timestamp'] = pd.to_datetime(sequence_df['timestamp'], unit='ms')
    sequence_df['timestamp'] = sequence_df['timestamp'].astype('int64') // 10**9  # UNIX 타임스탬프 (초 단위)

    # 새로운 데이터 인코딩
    sequence_df['package_encoded'] = sequence_df['package_name'].apply(
        lambda x: package_label_encoder.transform([x])[0] if x in df['package_name'].values else -1
    )
    sequence_df['category_encoded'] = category_label_encoder.transform(sequence_df['category'])

    # 시퀀스 데이터 준비
    encoded_packages = sequence_df['package_encoded'].tolist()
    encoded_categories = sequence_df['category_encoded'].tolist()
    encoded_timestamps = sequence_df['timestamp'].tolist()

    max_len = 50  # 모델 학습 시 사용한 max_len과 동일해야 함
    new_sequence_packages_padded = pad_sequences([encoded_packages], maxlen=max_len, padding='post')
    new_sequence_categories_padded = pad_sequences([encoded_categories], maxlen=max_len, padding='post')
    new_sequence_timestamps_padded = pad_sequences([encoded_timestamps], maxlen=max_len, padding='post')

    # 차원 변경
    new_sequence_packages_padded = np.expand_dims(new_sequence_packages_padded, -1)
    new_sequence_categories_padded = np.expand_dims(new_sequence_categories_padded, -1)
    new_sequence_timestamps_padded = np.expand_dims(new_sequence_timestamps_padded, -1)

    # LSTM, GRU, RNN 모델로 패키지 예측
    lstm_predictions = lstm_model.predict([new_sequence_packages_padded, new_sequence_categories_padded, new_sequence_timestamps_padded])
    gru_predictions = gru_model.predict([new_sequence_packages_padded, new_sequence_categories_padded, new_sequence_timestamps_padded])
    rnn_predictions = rnn_model.predict([new_sequence_packages_padded, new_sequence_categories_padded, new_sequence_timestamps_padded])

    # 소프트 보팅 앙상블: 모델들의 예측 확률을 평균
    ensemble_predictions = (lstm_predictions + gru_predictions + rnn_predictions) / 3

    # 예측 결과에서 상위 패키지 인덱스 추출
    top_n_indices = ensemble_predictions[0].argsort()[::-1]  # 상위 인덱스 추출 (내림차순)

    # 예측된 패키지 및 카테고리 중에서 'Not Found'가 아닌 것만 선택
    predicted_results = []
    for index in top_n_indices:
        if index == -1:  # 패키지 정보가 없는 경우
            continue
    
        # 새로운 인덱스가 학습되지 않은 경우를 처리
        try:
            package = package_label_encoder.inverse_transform([index])[0]  # 패키지명 디코딩
        except ValueError:  # 학습되지 않은 인덱스가 나올 경우
            package = 'Unknown'
    
        if package != 'Unknown':
            category = df[df['package_name'] == package]['category'].values[0]  # 해당 패키지에 대한 카테고리 추출
            if category != 'Not Found':  # 카테고리가 'Not Found'가 아닌 경우에만 추가
                predicted_results.append({
                    "package_name": package,
                    "category": category
                })

        if len(predicted_results) >= 4:  # 4개의 결과만 선택
            break

    # 패키지 정보가 없을 경우 카테고리 정보로 예측 추가
    if len(predicted_results) < 4:
        remaining_count = 4 - len(predicted_results)
        additional_categories = sequence_df[sequence_df['package_encoded'] == -1]['category'].unique()[:remaining_count]
    
        for category in additional_categories:
            predicted_results.append({
                "package_name": "Unknown",  # 패키지 정보가 없을 경우
                "category": category
            })

    # 최종 예측 결과 반환
    return jsonify({'apps': predicted_results})


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)  # 서버 실행
