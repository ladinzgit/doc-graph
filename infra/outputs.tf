output "alb_dns_name" {
  description = "Notion Webhook 등록 URL. CloudFront를 거치지 않고 ALB로 직접 수신."
  value       = module.alb.dns_name
}

# React 번들 완성 후 주석 해제
# output "cloudfront_domain" {
#   description = "React 프론트엔드 + API 접속 URL (CloudFront). /api/*는 ALB로 프록시됨."
#   value       = module.s3_cf.cloudfront_domain
# }

output "swagger_ui_url" {
  description = "Swagger UI 접속 주소. context-path(/api) 포함."
  value       = "http://${module.alb.dns_name}/api/swagger-ui/index.html"
}

output "ecr_repository_url" {
  description = "Docker 이미지 push 대상 URL"
  value       = module.ecr.repository_url
}

output "rds_endpoint" {
  description = "Spring Boot DB 연결 주소"
  value       = module.rds.endpoint
  sensitive   = true
}
